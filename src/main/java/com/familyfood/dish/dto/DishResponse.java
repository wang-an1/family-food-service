package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishIngredient;
import com.familyfood.dish.entity.DishTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "菜品详情响应")
public record DishResponse(
        @Schema(description = "菜品 ID", example = "1")
        Long id,
        @Schema(description = "菜品名称", example = "番茄炒蛋")
        String name,
        @Schema(description = "菜品别名", example = "西红柿炒鸡蛋")
        String aliases,
        @Schema(description = "菜品描述", example = "家常快手菜")
        String description,
        @Schema(description = "菜品图片 URL", example = "/uploads/dish/tomato-egg.jpg")
        String imageUrl,
        @Schema(description = "分类 ID", example = "1")
        Long categoryId,
        @Schema(description = "分类名称", example = "家常菜")
        String categoryName,
        @Schema(description = "标签列表")
        List<DishTag> tags,
        @Schema(description = "适用餐次", example = "[\"LUNCH\",\"DINNER\"]")
        List<String> mealTypes,
        @Schema(description = "口味", example = "咸鲜")
        String taste,
        @Schema(description = "制作难度", example = "EASY")
        String difficulty,
        @Schema(description = "预计制作分钟数", example = "15")
        Integer estimatedMinutes,
        @Schema(description = "默认份数", example = "2")
        BigDecimal defaultServings,
        @Schema(description = "制作步骤")
        String instructions,
        @Schema(description = "来源类型", example = "MANUAL")
        String sourceType,
        @Schema(description = "来源 URL")
        String sourceUrl,
        @Schema(description = "菜品状态", example = "ACTIVE")
        String status,
        @Schema(description = "食材列表")
        List<DishIngredient> ingredients
) {
}
