package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "AI 菜单计划请求")
public record MenuPlanRequest(
        @Schema(description = "菜单生成要求", example = "两大一小晚餐，清淡少油")
        @NotBlank @Size(max = 2000)
        String prompt,
        @Schema(description = "餐次类型", allowableValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"}, example = "DINNER")
        @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK|CUSTOM")
        String mealType,
        @Schema(description = "就餐人数", example = "3")
        @Min(1) @Max(20)
        Integer peopleCount,
        @Schema(description = "偏好菜品 ID 列表", example = "[1,2]")
        List<Long> preferredDishIds,
        @Schema(description = "忌口或避讳", example = "不吃香菜")
        @Size(max = 500)
        String avoidances
) {
}
