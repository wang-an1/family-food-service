package com.familyfood.system.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ScheduledTaskResponse(
        Long id,
        String name,
        String taskType,
        String taskName,
        String status,
        String scheduleMode,
        String cronExpression,
        ScheduleConfig scheduleConfig,
        Map<String, Object> parameters,
        String description,
        String runtimeState,
        LocalDateTime previousFireTime,
        LocalDateTime nextFireTime,
        List<LocalDateTime> nextFireTimes,
        boolean handlerAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy) {
}
