package com.familyfood.system.dto;

public record ScheduledTaskParameterDefinition(
        String key,
        String label,
        String type,
        boolean required,
        String description,
        Object defaultValue) {
}
