package com.familyfood.system.dto;

import java.util.List;

public record ScheduleConfig(
        String runAt,
        String startAt,
        Integer interval,
        String intervalUnit,
        String timeOfDay,
        List<Integer> daysOfWeek,
        Integer dayOfMonth,
        String timeZone) {
}
