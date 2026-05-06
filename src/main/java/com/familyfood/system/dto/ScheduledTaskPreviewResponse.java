package com.familyfood.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduledTaskPreviewResponse(
        String scheduleMode,
        String cronExpression,
        List<LocalDateTime> nextFireTimes) {
}
