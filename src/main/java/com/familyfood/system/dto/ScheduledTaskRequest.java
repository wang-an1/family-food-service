package com.familyfood.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record ScheduledTaskRequest(
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(max = 64) String taskType,
        @NotBlank @Size(max = 16) String status,
        @NotBlank @Size(max = 16) String scheduleMode,
        @Size(max = 120) String cronExpression,
        @Valid @NotNull ScheduleConfig scheduleConfig,
        Map<String, Object> parameters,
        @Size(max = 500) String description) {
}
