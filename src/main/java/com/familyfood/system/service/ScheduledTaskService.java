package com.familyfood.system.service;

import com.familyfood.system.dto.ScheduledTaskPreviewRequest;
import com.familyfood.system.dto.ScheduledTaskPreviewResponse;
import com.familyfood.system.dto.ScheduledTaskRequest;
import com.familyfood.system.dto.ScheduledTaskResponse;
import com.familyfood.system.dto.ScheduledTaskRunLogResponse;
import com.familyfood.system.dto.ScheduledTaskTypeResponse;
import java.util.List;

public interface ScheduledTaskService {
    List<ScheduledTaskTypeResponse> types();

    ScheduledTaskPreviewResponse preview(ScheduledTaskPreviewRequest request);

    List<ScheduledTaskResponse> list(String status, String taskType);

    ScheduledTaskResponse create(ScheduledTaskRequest request);

    ScheduledTaskResponse update(Long id, ScheduledTaskRequest request);

    ScheduledTaskResponse enable(Long id);

    ScheduledTaskResponse disable(Long id);

    ScheduledTaskResponse triggerNow(Long id);

    void delete(Long id);

    List<ScheduledTaskRunLogResponse> logs(Long id);

    void reconcileAll();

    void executeScheduledTask(Long scheduledTaskId, boolean manual);
}
