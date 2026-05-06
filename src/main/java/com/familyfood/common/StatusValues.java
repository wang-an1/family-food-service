package com.familyfood.common;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class StatusValues {
    public static final Set<String> DISH_STATUSES = names(Enums.DishStatus.values());
    public static final Set<String> MEAL_STATUSES = names(Enums.MealStatus.values());
    public static final Set<String> MEAL_TYPES = names(Enums.MealType.values());
    public static final Set<String> ORDER_STATUSES = names(Enums.OrderStatus.values());
    public static final Set<String> AI_TASK_STATUSES = names(Enums.AiTaskStatus.values());
    public static final Set<String> SOURCE_TYPES = names(Enums.SourceType.values());
    public static final Set<String> SCHEDULED_TASK_STATUSES = names(Enums.ScheduledTaskStatus.values());
    public static final Set<String> SCHEDULED_RUN_STATUSES = names(Enums.ScheduledRunStatus.values());
    public static final Set<String> SCHEDULE_MODES = names(Enums.ScheduleMode.values());

    private StatusValues() {
    }

    public static String optional(String value, Set<String> allowed, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return required(value, allowed, field);
    }

    public static String orDefault(String value, String defaultValue, Set<String> allowed, String field) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return required(value, allowed, field);
    }

    public static String required(String value, Set<String> allowed, String field) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw AppException.validation(FieldNames.displayName(field) + "取值不合法，请重新选择");
        }
        return normalized;
    }

    private static <E extends Enum<E>> Set<String> names(E[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toUnmodifiableSet());
    }
}
