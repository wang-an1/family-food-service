package com.familyfood.system.scheduler;

import com.familyfood.system.dto.ScheduledTaskParameterDefinition;
import java.util.List;

public interface ScheduledTaskHandler {
    String type();

    String name();

    default String description() {
        return "";
    }

    default List<ScheduledTaskParameterDefinition> parameterDefinitions() {
        return List.of();
    }

    void execute(ScheduledTaskExecutionContext context);
}
