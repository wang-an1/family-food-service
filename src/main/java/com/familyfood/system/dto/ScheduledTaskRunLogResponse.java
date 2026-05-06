package com.familyfood.system.dto;

import java.time.LocalDateTime;

public record ScheduledTaskRunLogResponse(
        Long id,
        Long scheduledTaskId,
        String triggerType,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationMillis,
        String errorMessage,
        LocalDateTime createdAt) {
}
