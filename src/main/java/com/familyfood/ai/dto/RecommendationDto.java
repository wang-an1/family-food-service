package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 推荐项")
public record RecommendationDto(
        @Schema(description = "推荐类型", allowableValues = {"DISH", "EXTRACTED_DISH"}, example = "DISH")
        String type,
        @Schema(description = "正式菜品 ID", example = "1")
        Long dishId,
        @Schema(description = "AI 抽取菜品草稿 ID", example = "10")
        Long extractedDishId,
        @Schema(description = "推荐标题", example = "番茄炒蛋")
        String title,
        @Schema(description = "推荐理由", example = "制作快，适合晚餐")
        String reason,
        @Schema(description = "推荐分数", example = "0.92")
        double score) {
}
