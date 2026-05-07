package com.familyfood.system.scheduler;

import com.familyfood.common.AppException;
import com.familyfood.common.FieldNames;
import com.familyfood.common.StatusValues;
import com.familyfood.system.dto.ScheduleConfig;
import com.familyfood.system.entity.ScheduledTask;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTaskSchedulePlanner {
    private static final String DEFAULT_ZONE = "Asia/Shanghai";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final int PREVIEW_LIMIT = 5;

    public PlannedSchedule plan(String scheduleMode, String cronExpression, ScheduleConfig config) {
        String mode = StatusValues.required(scheduleMode, StatusValues.SCHEDULE_MODES, "scheduleMode");
        ScheduleConfig safeConfig = config == null ? new ScheduleConfig(null, null, null, null, null, null, null, null) : config;
        ZoneId zone = zone(safeConfig.timeZone());
        return switch (mode) {
            case "ONCE" -> new PlannedSchedule(mode, null, safeConfig, zone);
            case "INTERVAL" -> intervalPlan(mode, safeConfig, zone);
            case "DAILY" -> timePlan(mode, toDailyCron(parseTime(safeConfig.timeOfDay())), safeConfig, zone);
            case "WEEKLY" -> weeklyPlan(mode, safeConfig, zone);
            case "MONTHLY" -> monthlyPlan(mode, safeConfig, zone);
            case "CRON" -> cronPlan(mode, cronExpression, safeConfig, zone);
            default -> throw AppException.validation("暂不支持这种计划模式，请重新选择");
        };
    }

    public Trigger buildTrigger(ScheduledTask task, PlannedSchedule schedule) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .withIdentity(task.getTriggerName(), task.getTriggerGroup())
                .forJob(new JobKey(task.getJobName(), task.getJobGroup()))
                .usingJobData("scheduledTaskId", task.getId());
        return switch (schedule.scheduleMode()) {
            case "ONCE" -> builder
                    .startAt(date(parseRunAt(schedule.config(), schedule.zoneId()), schedule.zoneId()))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0)
                            .withMisfireHandlingInstructionFireNow())
                    .build();
            case "INTERVAL" -> intervalTrigger(builder, schedule);
            default -> builder
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.cronExpression())
                            .inTimeZone(TimeZone.getTimeZone(schedule.zoneId()))
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
        };
    }

    public List<LocalDateTime> nextFireTimes(PlannedSchedule schedule) {
        return switch (schedule.scheduleMode()) {
            case "ONCE" -> List.of(parseRunAt(schedule.config(), schedule.zoneId()));
            case "INTERVAL" -> intervalPreview(schedule);
            default -> cronPreview(schedule.cronExpression(), schedule.zoneId(), PREVIEW_LIMIT);
        };
    }

    private PlannedSchedule intervalPlan(String mode, ScheduleConfig config, ZoneId zone) {
        int interval = requirePositive(config.interval(), "scheduleConfig.interval");
        String unit = config.intervalUnit() == null ? "" : config.intervalUnit().trim().toUpperCase(Locale.ROOT);
        if (!List.of("MINUTES", "HOURS").contains(unit)) {
            throw AppException.validation("间隔单位只能选择分钟或小时");
        }
        String cron = "MINUTES".equals(unit)
                ? "0 0/" + interval + " * * * ?"
                : "0 0 0/" + interval + " * * ?";
        return new PlannedSchedule(mode, cron, config, zone);
    }

    private PlannedSchedule timePlan(String mode, String cron, ScheduleConfig config, ZoneId zone) {
        return new PlannedSchedule(mode, cron, config, zone);
    }

    private PlannedSchedule weeklyPlan(String mode, ScheduleConfig config, ZoneId zone) {
        LocalTime time = parseTime(config.timeOfDay());
        List<Integer> days = config.daysOfWeek();
        if (days == null || days.isEmpty()) {
            throw AppException.validation("请选择每周执行日期");
        }
        String dayExpression = days.stream()
                .map(this::dayName)
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElseThrow(() -> AppException.validation("每周执行日期必须在 1 到 7 之间"));
        return new PlannedSchedule(mode,
                "0 " + time.getMinute() + " " + time.getHour() + " ? * " + dayExpression,
                config,
                zone);
    }

    private PlannedSchedule monthlyPlan(String mode, ScheduleConfig config, ZoneId zone) {
        LocalTime time = parseTime(config.timeOfDay());
        int day = requirePositive(config.dayOfMonth(), "scheduleConfig.dayOfMonth");
        if (day > 31) {
            throw AppException.validation("每月执行日期需要在 1 到 31 之间");
        }
        return new PlannedSchedule(mode,
                "0 " + time.getMinute() + " " + time.getHour() + " " + day + " * ?",
                config,
                zone);
    }

    private PlannedSchedule cronPlan(String mode, String cronExpression, ScheduleConfig config, ZoneId zone) {
        if (cronExpression == null || cronExpression.isBlank()) {
            throw AppException.validation("请填写 Cron 表达式");
        }
        String cron = cronExpression.trim();
        if (!CronExpression.isValidExpression(cron)) {
            throw AppException.validation("Cron 表达式格式不正确，请检查后再试");
        }
        if (cronPreview(cron, zone, 1).isEmpty()) {
            throw AppException.validation("这个 Cron 表达式不会再触发，请调整执行时间");
        }
        return new PlannedSchedule(mode, cron, config, zone);
    }

    private Trigger intervalTrigger(TriggerBuilder<Trigger> builder, PlannedSchedule schedule) {
        int interval = requirePositive(schedule.config().interval(), "scheduleConfig.interval");
        String unit = schedule.config().intervalUnit().trim().toUpperCase(Locale.ROOT);
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .repeatForever()
                .withMisfireHandlingInstructionNextWithRemainingCount();
        scheduleBuilder = "MINUTES".equals(unit)
                ? scheduleBuilder.withIntervalInMinutes(interval)
                : scheduleBuilder.withIntervalInHours(interval);
        LocalDateTime startAt = parseOptionalDateTime(schedule.config().startAt(), schedule.zoneId());
        if (startAt == null) {
            return builder.startNow().withSchedule(scheduleBuilder).build();
        }
        return builder.startAt(date(startAt, schedule.zoneId())).withSchedule(scheduleBuilder).build();
    }

    private List<LocalDateTime> intervalPreview(PlannedSchedule schedule) {
        int interval = requirePositive(schedule.config().interval(), "scheduleConfig.interval");
        String unit = schedule.config().intervalUnit().trim().toUpperCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now(schedule.zoneId());
        LocalDateTime next = parseOptionalDateTime(schedule.config().startAt(), schedule.zoneId());
        if (next == null || !next.isAfter(now)) {
            next = now;
        }
        List<LocalDateTime> times = new ArrayList<>();
        for (int i = 0; i < PREVIEW_LIMIT; i++) {
            if (i > 0 || !next.isBefore(now)) {
                times.add(next);
            }
            next = "MINUTES".equals(unit) ? next.plusMinutes(interval) : next.plusHours(interval);
        }
        return times;
    }

    private List<LocalDateTime> cronPreview(String cron, ZoneId zone, int limit) {
        try {
            CronExpression expression = new CronExpression(cron);
            expression.setTimeZone(TimeZone.getTimeZone(zone));
            Date cursor = new Date();
            List<LocalDateTime> times = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                Date next = expression.getNextValidTimeAfter(cursor);
                if (next == null) {
                    break;
                }
                times.add(LocalDateTime.ofInstant(next.toInstant(), zone));
                cursor = next;
            }
            return times;
        } catch (ParseException ex) {
            throw AppException.validation("Cron 表达式格式不正确，请检查后再试");
        }
    }

    private LocalDateTime parseRunAt(ScheduleConfig config, ZoneId zone) {
        LocalDateTime runAt = parseOptionalDateTime(config.runAt(), zone);
        if (runAt == null) {
            throw AppException.validation("请填写执行时间");
        }
        if (!runAt.isAfter(LocalDateTime.now(zone))) {
            throw AppException.validation("执行时间需要晚于当前时间");
        }
        return runAt;
    }

    private LocalDateTime parseOptionalDateTime(String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeException ex) {
            throw AppException.validation("日期时间格式不正确，请使用类似 2026-04-29T18:30:00 的格式");
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            throw AppException.validation("请填写执行时间");
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (DateTimeException ex) {
            throw AppException.validation("执行时间格式不正确，请使用 HH:mm 格式");
        }
    }

    private String toDailyCron(LocalTime time) {
        return "0 " + time.getMinute() + " " + time.getHour() + " * * ?";
    }

    private int requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw AppException.validation(FieldNames.displayName(field) + "需要大于 0");
        }
        return value;
    }

    private String dayName(Integer value) {
        return switch (requirePositive(value, "scheduleConfig.daysOfWeek")) {
            case 1 -> "MON";
            case 2 -> "TUE";
            case 3 -> "WED";
            case 4 -> "THU";
            case 5 -> "FRI";
            case 6 -> "SAT";
            case 7 -> "SUN";
            default -> throw AppException.validation("每周执行日期必须在 1 到 7 之间");
        };
    }

    private ZoneId zone(String timeZone) {
        try {
            return ZoneId.of(timeZone == null || timeZone.isBlank() ? DEFAULT_ZONE : timeZone.trim());
        } catch (DateTimeException ex) {
            throw AppException.validation("时区格式不正确，请检查后再试");
        }
    }

    private Date date(LocalDateTime localDateTime, ZoneId zone) {
        return Date.from(localDateTime.atZone(zone).toInstant());
    }

    public record PlannedSchedule(
            String scheduleMode,
            String cronExpression,
            ScheduleConfig config,
            ZoneId zoneId) {
    }
}
