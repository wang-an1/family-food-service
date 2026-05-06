package com.familyfood.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "餐次创建请求")
public record MealSessionRequest(
        @Schema(description = "餐次标题", example = "周三晚餐")
        @NotBlank @Size(max = 100)
        String title,
        @Schema(description = "餐次类型", allowableValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"}, example = "DINNER")
        @NotBlank @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK|CUSTOM")
        String mealType,
        @Schema(description = "就餐日期", format = "date", example = "2026-04-29")
        LocalDate mealDate,
        @Schema(description = "期望就餐时间", format = "date-time", example = "2026-04-29T18:30:00")
        LocalDateTime expectedTime,
        @Schema(description = "餐次状态", allowableValues = {"OPEN", "LOCKED", "COMPLETED", "CANCELLED"}, example = "OPEN")
        @Pattern(regexp = "OPEN|LOCKED|COMPLETED|CANCELLED")
        String status,
        @Schema(description = "是否需要管理员确认", example = "true")
        Boolean confirmRequired
) {
}
