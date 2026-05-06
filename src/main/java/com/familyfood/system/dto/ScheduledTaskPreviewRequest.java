package com.familyfood.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScheduledTaskPreviewRequest(
        @NotBlank @Size(max = 16) String scheduleMode,
        @Size(max = 120) String cronExpression,
        @Valid @NotNull ScheduleConfig scheduleConfig) {
}
