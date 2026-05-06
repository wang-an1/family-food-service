package com.familyfood.system.scheduler;

import java.util.Map;

public record ScheduledTaskExecutionContext(
        Long scheduledTaskId,
        String taskType,
        Map<String, Object> parameters,
        boolean manual) {
}
