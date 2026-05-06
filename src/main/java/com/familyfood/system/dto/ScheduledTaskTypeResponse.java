package com.familyfood.system.dto;

import java.util.List;

public record ScheduledTaskTypeResponse(
        String type,
        String name,
        String description,
        List<ScheduledTaskParameterDefinition> parameterDefinitions) {
}
