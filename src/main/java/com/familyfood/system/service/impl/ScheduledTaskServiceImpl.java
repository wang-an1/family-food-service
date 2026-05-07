package com.familyfood.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.auth.security.CurrentUser;
import com.familyfood.auth.security.UserPrincipal;
import com.familyfood.common.AppException;
import com.familyfood.common.Enums.Role;
import com.familyfood.common.StatusValues;
import com.familyfood.system.dao.ScheduledTaskMapper;
import com.familyfood.system.dao.ScheduledTaskRunLogMapper;
import com.familyfood.system.dto.ScheduleConfig;
import com.familyfood.system.dto.ScheduledTaskPreviewRequest;
import com.familyfood.system.dto.ScheduledTaskPreviewResponse;
import com.familyfood.system.dto.ScheduledTaskRequest;
import com.familyfood.system.dto.ScheduledTaskResponse;
import com.familyfood.system.dto.ScheduledTaskRunLogResponse;
import com.familyfood.system.dto.ScheduledTaskTypeResponse;
import com.familyfood.system.entity.ScheduledTask;
import com.familyfood.system.entity.ScheduledTaskRunLog;
import com.familyfood.system.scheduler.DelegatingScheduledTaskJob;
import com.familyfood.system.scheduler.ScheduledTaskExecutionContext;
import com.familyfood.system.scheduler.ScheduledTaskHandler;
import com.familyfood.system.scheduler.ScheduledTaskHandlerRegistry;
import com.familyfood.system.scheduler.ScheduledTaskSchedulePlanner.PlannedSchedule;
import com.familyfood.system.scheduler.ScheduledTaskSchedulePlanner;
import com.familyfood.system.service.ScheduledTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ScheduledTaskServiceImpl implements ScheduledTaskService {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskServiceImpl.class);
    private static final String GROUP = "SYSTEM";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ScheduledTaskMapper taskMapper;
    private final ScheduledTaskRunLogMapper runLogMapper;
    private final ScheduledTaskHandlerRegistry registry;
    private final ScheduledTaskSchedulePlanner planner;
    private final Scheduler scheduler;
    private final ObjectMapper objectMapper;

    @Autowired
    public ScheduledTaskServiceImpl(ScheduledTaskMapper taskMapper,
                                ScheduledTaskRunLogMapper runLogMapper,
                                ScheduledTaskHandlerRegistry registry,
                                ScheduledTaskSchedulePlanner planner,
                                Scheduler scheduler,
                                ObjectMapper objectMapper) {
        this.taskMapper = taskMapper;
        this.runLogMapper = runLogMapper;
        this.registry = registry;
        this.planner = planner;
        this.scheduler = scheduler;
        this.objectMapper = objectMapper;
    }

    public List<ScheduledTaskTypeResponse> types() {
        requireGlobalAdmin();
        return registry.all().stream()
                .map(handler -> new ScheduledTaskTypeResponse(
                        handler.type(), handler.name(), handler.description(), handler.parameterDefinitions()))
                .toList();
    }

    public ScheduledTaskPreviewResponse preview(ScheduledTaskPreviewRequest request) {
        requireGlobalAdmin();
        PlannedSchedule planned = planner.plan(request.scheduleMode(), request.cronExpression(), request.scheduleConfig());
        return new ScheduledTaskPreviewResponse(
                planned.scheduleMode(), planned.cronExpression(), planner.nextFireTimes(planned));
    }

    public List<ScheduledTaskResponse> list(String status, String taskType) {
        requireGlobalAdmin();
        QueryWrapper<ScheduledTask> wrapper = new QueryWrapper<ScheduledTask>().orderByDesc("updated_at");
        String normalizedStatus = StatusValues.optional(status, StatusValues.SCHEDULED_TASK_STATUSES, "status");
        if (normalizedStatus != null) {
            wrapper.eq("status", normalizedStatus);
        }
        if (taskType != null && !taskType.isBlank()) {
            wrapper.eq("task_type", normalizeType(taskType));
        }
        return taskMapper.selectList(wrapper).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ScheduledTaskResponse create(ScheduledTaskRequest request) {
        UserPrincipal principal = requireGlobalAdmin();
        String type = normalizeType(request.taskType());
        requireHandler(type);
        String status = StatusValues.required(request.status(), StatusValues.SCHEDULED_TASK_STATUSES, "status");
        PlannedSchedule planned = planner.plan(request.scheduleMode(), request.cronExpression(), request.scheduleConfig());
        LocalDateTime now = LocalDateTime.now();
        String token = UUID.randomUUID().toString().replace("-", "");

        ScheduledTask task = new ScheduledTask();
        task.setName(request.name().trim());
        task.setTaskType(type);
        task.setStatus(status);
        task.setScheduleMode(planned.scheduleMode());
        task.setCronExpression(planned.cronExpression());
        task.setScheduleConfigJson(toJson(request.scheduleConfig()));
        task.setParametersJson(toJson(safeMap(request.parameters())));
        task.setDescription(blankToNull(request.description()));
        task.setJobName("scheduled-task-" + token);
        task.setJobGroup(GROUP);
        task.setTriggerName("scheduled-task-trigger-" + token);
        task.setTriggerGroup(GROUP);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setCreatedBy(principal.userId());
        task.setUpdatedBy(principal.userId());
        taskMapper.insert(task);
        syncToQuartz(task);
        return toResponse(taskMapper.selectById(task.getId()));
    }

    @Transactional
    public ScheduledTaskResponse update(Long id, ScheduledTaskRequest request) {
        UserPrincipal principal = requireGlobalAdmin();
        ScheduledTask task = requireTask(id);
        String type = normalizeType(request.taskType());
        requireHandler(type);
        String status = StatusValues.required(request.status(), StatusValues.SCHEDULED_TASK_STATUSES, "status");
        PlannedSchedule planned = planner.plan(request.scheduleMode(), request.cronExpression(), request.scheduleConfig());

        task.setName(request.name().trim());
        task.setTaskType(type);
        task.setStatus(status);
        task.setScheduleMode(planned.scheduleMode());
        task.setCronExpression(planned.cronExpression());
        task.setScheduleConfigJson(toJson(request.scheduleConfig()));
        task.setParametersJson(toJson(safeMap(request.parameters())));
        task.setDescription(blankToNull(request.description()));
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(principal.userId());
        taskMapper.updateById(task);
        syncToQuartz(task);
        return toResponse(taskMapper.selectById(task.getId()));
    }

    @Transactional
    public ScheduledTaskResponse enable(Long id) {
        UserPrincipal principal = requireGlobalAdmin();
        ScheduledTask task = requireTask(id);
        requireHandler(task.getTaskType());
        task.setStatus("ENABLED");
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(principal.userId());
        taskMapper.updateById(task);
        syncToQuartz(task);
        return toResponse(taskMapper.selectById(task.getId()));
    }

    @Transactional
    public ScheduledTaskResponse disable(Long id) {
        UserPrincipal principal = requireGlobalAdmin();
        ScheduledTask task = requireTask(id);
        task.setStatus("DISABLED");
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(principal.userId());
        taskMapper.updateById(task);
        pauseTrigger(task);
        return toResponse(taskMapper.selectById(task.getId()));
    }

    @Transactional
    public ScheduledTaskResponse triggerNow(Long id) {
        requireGlobalAdmin();
        ScheduledTask task = requireTask(id);
        requireHandler(task.getTaskType());
        try {
            if (!scheduler.checkExists(jobKey(task))) {
                syncToQuartz(task);
            }
            executeScheduledTask(task.getId(), true);
            return toResponse(task);
        } catch (SchedulerException ex) {
            log.warn("scheduled_task_trigger_failed taskId={}", id, ex);
            throw AppException.serviceUnavailable("SCHEDULER_ERROR", "定时任务暂时触发失败，请稍后再试");
        }
    }

    @Transactional
    public void delete(Long id) {
        requireGlobalAdmin();
        ScheduledTask task = requireTask(id);
        try {
            scheduler.unscheduleJob(triggerKey(task));
            scheduler.deleteJob(jobKey(task));
        } catch (SchedulerException ex) {
            throw AppException.serviceUnavailable("SCHEDULER_ERROR", "定时任务暂时删除失败，请稍后再试");
        }
        taskMapper.deleteById(id);
    }

    public List<ScheduledTaskRunLogResponse> logs(Long id) {
        requireGlobalAdmin();
        requireTask(id);
        return runLogMapper.selectList(new QueryWrapper<ScheduledTaskRunLog>()
                        .eq("scheduled_task_id", id)
                        .orderByDesc("created_at")
                        .orderByDesc("id")
                        .last("limit 100"))
                .stream().map(this::toRunLogResponse).toList();
    }

    @Transactional
    public void reconcileAll() {
        List<ScheduledTask> tasks = taskMapper.selectList(new QueryWrapper<ScheduledTask>().orderByAsc("id"));
        for (ScheduledTask task : tasks) {
            try {
                if (!registry.has(task.getTaskType())) {
                    pauseJobIfPresent(task);
                    continue;
                }
                syncToQuartz(task);
            } catch (Exception ex) {
                log.warn("scheduled_task_reconcile_failed taskId={}", task.getId(), ex);
            }
        }
    }

    @Transactional
    public void executeScheduledTask(Long scheduledTaskId, boolean manual) {
        ScheduledTask task = taskMapper.selectById(scheduledTaskId);
        if (task == null || (!manual && !"ENABLED".equals(task.getStatus()))) {
            return;
        }
        ScheduledTaskRunLog runLog = new ScheduledTaskRunLog();
        LocalDateTime started = LocalDateTime.now();
        runLog.setScheduledTaskId(task.getId());
        runLog.setTriggerType(manual ? "MANUAL" : "SCHEDULED");
        runLog.setStatus("RUNNING");
        runLog.setStartedAt(started);
        runLog.setCreatedAt(started);
        runLogMapper.insert(runLog);

        try {
            ScheduledTaskHandler handler = registry.get(task.getTaskType());
            if (handler == null) {
                throw new IllegalStateException("缺少对应的定时任务处理器：" + task.getTaskType());
            }
            handler.execute(new ScheduledTaskExecutionContext(
                    task.getId(), task.getTaskType(), parseMap(task.getParametersJson()), manual));
            finishRun(runLog, "SUCCESS", null);
        } catch (Exception ex) {
            log.warn("scheduled_task_execution_failed taskId={} manual={}", task.getId(), manual, ex);
            finishRun(runLog, "FAILED", shortMessage(ex));
        }
    }

    private void syncToQuartz(ScheduledTask task) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(DelegatingScheduledTaskJob.class)
                    .withIdentity(jobKey(task))
                    .usingJobData("scheduledTaskId", task.getId())
                    .storeDurably(true)
                    .requestRecovery(false)
                    .build();
            scheduler.addJob(jobDetail, true, true);
            PlannedSchedule planned = planner.plan(task.getScheduleMode(), task.getCronExpression(), parseScheduleConfig(task));
            Trigger trigger = planner.buildTrigger(task, planned);
            if (scheduler.checkExists(triggerKey(task))) {
                scheduler.rescheduleJob(triggerKey(task), trigger);
            } else {
                scheduler.scheduleJob(trigger);
            }
            if ("DISABLED".equals(task.getStatus())) {
                scheduler.pauseTrigger(triggerKey(task));
            } else {
                scheduler.resumeTrigger(triggerKey(task));
            }
        } catch (SchedulerException ex) {
            throw AppException.serviceUnavailable("SCHEDULER_ERROR", "定时任务暂时同步失败，请稍后再试");
        }
    }

    private void pauseTrigger(ScheduledTask task) {
        try {
            if (scheduler.checkExists(triggerKey(task))) {
                scheduler.pauseTrigger(triggerKey(task));
            }
        } catch (SchedulerException ex) {
            throw AppException.serviceUnavailable("SCHEDULER_ERROR", "定时任务暂时暂停失败，请稍后再试");
        }
    }

    private void pauseJobIfPresent(ScheduledTask task) throws SchedulerException {
        if (scheduler.checkExists(jobKey(task))) {
            scheduler.pauseJob(jobKey(task));
        }
    }

    private void finishRun(ScheduledTaskRunLog runLog, String status, String errorMessage) {
        LocalDateTime finished = LocalDateTime.now();
        runLog.setStatus(status);
        runLog.setFinishedAt(finished);
        runLog.setDurationMillis(Duration.between(runLog.getStartedAt(), finished).toMillis());
        runLog.setErrorMessage(errorMessage);
        runLogMapper.updateById(runLog);
    }

    private ScheduledTaskResponse toResponse(ScheduledTask task) {
        boolean handlerAvailable = registry.has(task.getTaskType());
        Trigger trigger = findTrigger(task);
        return new ScheduledTaskResponse(
                task.getId(),
                task.getName(),
                task.getTaskType(),
                taskName(task),
                task.getStatus(),
                task.getScheduleMode(),
                task.getCronExpression(),
                parseScheduleConfig(task),
                parseMap(task.getParametersJson()),
                task.getDescription(),
                runtimeState(task, handlerAvailable),
                toLocalDateTime(trigger == null ? null : trigger.getPreviousFireTime()),
                toLocalDateTime(trigger == null ? null : trigger.getNextFireTime()),
                previewSafely(task),
                handlerAvailable,
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getCreatedBy(),
                task.getUpdatedBy());
    }

    private ScheduledTaskRunLogResponse toRunLogResponse(ScheduledTaskRunLog runLog) {
        return new ScheduledTaskRunLogResponse(
                runLog.getId(),
                runLog.getScheduledTaskId(),
                runLog.getTriggerType(),
                runLog.getStatus(),
                runLog.getStartedAt(),
                runLog.getFinishedAt(),
                runLog.getDurationMillis(),
                runLog.getErrorMessage(),
                runLog.getCreatedAt());
    }

    private String runtimeState(ScheduledTask task, boolean handlerAvailable) {
        if (!handlerAvailable) {
            return "MISSING_HANDLER";
        }
        try {
            if (!scheduler.checkExists(triggerKey(task))) {
                return "MISSING_QUARTZ";
            }
            return scheduler.getTriggerState(triggerKey(task)).name();
        } catch (SchedulerException ex) {
            return "UNKNOWN";
        }
    }

    private Trigger findTrigger(ScheduledTask task) {
        try {
            return scheduler.checkExists(triggerKey(task)) ? scheduler.getTrigger(triggerKey(task)) : null;
        } catch (SchedulerException ex) {
            return null;
        }
    }

    private List<LocalDateTime> previewSafely(ScheduledTask task) {
        try {
            PlannedSchedule planned = planner.plan(task.getScheduleMode(), task.getCronExpression(), parseScheduleConfig(task));
            return planner.nextFireTimes(planned);
        } catch (AppException ex) {
            return List.of();
        }
    }

    private String taskName(ScheduledTask task) {
        ScheduledTaskHandler handler = registry.get(task.getTaskType());
        return handler == null ? null : handler.name();
    }

    private ScheduledTask requireTask(Long id) {
        ScheduledTask task = taskMapper.selectById(id);
        if (task == null) {
            throw AppException.notFound("未找到这个定时任务，请刷新后再试");
        }
        return task;
    }

    private void requireHandler(String type) {
        if (!registry.has(type)) {
            throw AppException.validation("当前定时任务类型不可用，请重新选择");
        }
    }

    private UserPrincipal requireGlobalAdmin() {
        UserPrincipal principal = CurrentUser.get();
        if (!Role.ADMIN.name().equals(principal.role()) || !"admin".equals(principal.username())) {
            throw AppException.forbidden("只有全局管理员可以管理定时任务");
        }
        return principal;
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private Map<String, Object> safeMap(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }

    private String toJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw AppException.validation("JSON 数据格式不正确，请检查后再保存");
        }
    }

    private ScheduleConfig parseScheduleConfig(ScheduledTask task) {
        if (task.getScheduleConfigJson() == null || task.getScheduleConfigJson().isBlank()) {
            return new ScheduleConfig(null, null, null, null, null, null, null, null);
        }
        try {
            return objectMapper.readValue(task.getScheduleConfigJson(), ScheduleConfig.class);
        } catch (JsonProcessingException ex) {
            throw AppException.validation("已保存的计划配置无法识别，请重新保存");
        }
    }

    private Map<String, Object> parseMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException ex) {
            throw AppException.validation("已保存的任务参数无法识别，请重新保存");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private JobKey jobKey(ScheduledTask task) {
        return new JobKey(task.getJobName(), task.getJobGroup());
    }

    private TriggerKey triggerKey(ScheduledTask task) {
        return new TriggerKey(task.getTriggerName(), task.getTriggerGroup());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private String shortMessage(Exception ex) {
        String message = ex instanceof AppException && ex.getMessage() != null
                ? ex.getMessage()
                : "任务执行失败，请查看服务日志";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
