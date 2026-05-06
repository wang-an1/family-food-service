package com.familyfood.common;

import java.util.Map;

public final class FieldNames {
    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("avoidances", "忌口说明"),
            Map.entry("bizType", "业务类型"),
            Map.entry("category", "分类"),
            Map.entry("categoryId", "分类"),
            Map.entry("color", "颜色"),
            Map.entry("config.key", "配置项"),
            Map.entry("config.value", "配置值"),
            Map.entry("configs", "配置列表"),
            Map.entry("confirmRequired", "是否需要确认"),
            Map.entry("cronExpression", "Cron 表达式"),
            Map.entry("dayOfMonth", "每月执行日期"),
            Map.entry("daysOfWeek", "每周执行日期"),
            Map.entry("description", "描述"),
            Map.entry("difficulty", "难度"),
            Map.entry("dishId", "菜品"),
            Map.entry("displayName", "显示名称"),
            Map.entry("estimatedMinutes", "预计用时"),
            Map.entry("expectedTime", "预计时间"),
            Map.entry("fallbackText", "补充文本"),
            Map.entry("imageUrl", "图片链接"),
            Map.entry("inputText", "输入内容"),
            Map.entry("instructions", "做法步骤"),
            Map.entry("interval", "执行间隔"),
            Map.entry("intervalUnit", "间隔单位"),
            Map.entry("items", "点餐菜品"),
            Map.entry("key", "配置项"),
            Map.entry("mealDate", "用餐日期"),
            Map.entry("mealSessionId", "餐次"),
            Map.entry("mealType", "餐别"),
            Map.entry("mealTypes", "适用餐别"),
            Map.entry("name", "名称"),
            Map.entry("note", "备注"),
            Map.entry("parameters", "任务参数"),
            Map.entry("password", "密码"),
            Map.entry("peopleCount", "用餐人数"),
            Map.entry("prompt", "需求描述"),
            Map.entry("quantity", "数量"),
            Map.entry("role", "角色"),
            Map.entry("runAt", "执行时间"),
            Map.entry("scheduleConfig", "计划配置"),
            Map.entry("scheduleConfig.dayOfMonth", "每月执行日期"),
            Map.entry("scheduleConfig.daysOfWeek", "每周执行日期"),
            Map.entry("scheduleConfig.interval", "执行间隔"),
            Map.entry("scheduleConfig.intervalUnit", "间隔单位"),
            Map.entry("scheduleConfig.runAt", "执行时间"),
            Map.entry("scheduleConfig.startAt", "开始时间"),
            Map.entry("scheduleConfig.timeOfDay", "执行时间"),
            Map.entry("scheduleConfig.timeZone", "时区"),
            Map.entry("scheduleMode", "计划模式"),
            Map.entry("sourceType", "来源类型"),
            Map.entry("sourceUrl", "来源链接"),
            Map.entry("startAt", "开始时间"),
            Map.entry("status", "状态"),
            Map.entry("tagIds", "标签"),
            Map.entry("taskType", "任务类型"),
            Map.entry("timeOfDay", "执行时间"),
            Map.entry("timeZone", "时区"),
            Map.entry("title", "标题"),
            Map.entry("unit", "单位"),
            Map.entry("url", "链接"),
            Map.entry("username", "用户名"),
            Map.entry("value", "配置值"),
            Map.entry("valueType", "配置值类型")
    );

    private FieldNames() {
    }

    public static String displayName(String path) {
        if (path == null || path.isBlank()) {
            return "参数";
        }
        String normalized = path.replaceAll("\\[\\d+\\]", "");
        String exact = NAMES.get(normalized);
        if (exact != null) {
            return exact;
        }
        int dot = normalized.lastIndexOf('.');
        String last = dot >= 0 ? normalized.substring(dot + 1) : normalized;
        return NAMES.getOrDefault(last, normalized);
    }
}
