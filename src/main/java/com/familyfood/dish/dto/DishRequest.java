package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "菜品保存请求")
public record DishRequest(
        @Schema(description = "分类 ID", example = "1")
        Long categoryId,
        @Schema(description = "菜品名称", example = "番茄炒蛋")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "菜品别名，多个别名可用逗号分隔", example = "西红柿炒鸡蛋")
        @Size(max = 200)
        String aliases,
        @Schema(description = "菜品描述", example = "家常快手菜")
        @Size(max = 1000)
        String description,
        @Schema(description = "菜品图片 URL", example = "/uploads/dish/tomato-egg.jpg")
        @Size(max = 500)
        String imageUrl,
        @Schema(description = "口味", example = "咸鲜")
        @Size(max = 50)
        String taste,
        @Schema(description = "适用餐次", example = "[\"LUNCH\",\"DINNER\"]")
        List<@NotBlank @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK|CUSTOM") String> mealTypes,
        @Schema(description = "制作难度", allowableValues = {"EASY", "MEDIUM", "HARD"}, example = "EASY")
        @Pattern(regexp = "EASY|MEDIUM|HARD")
        String difficulty,
        @Schema(description = "预计制作分钟数", example = "15")
        @Min(1)
        Integer estimatedMinutes,
        @Schema(description = "默认份数", example = "2")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal defaultServings,
        @Schema(description = "制作步骤", example = "鸡蛋炒熟后加入番茄翻炒调味")
        @Size(max = 4000)
        String instructions,
        @Schema(description = "来源类型", allowableValues = {"MANUAL", "AI", "LINK"}, example = "MANUAL")
        @Pattern(regexp = "MANUAL|AI|LINK")
        String sourceType,
        @Schema(description = "来源 URL", example = "https://example.com/recipe")
        @Size(max = 1000)
        String sourceUrl,
        @Schema(description = "菜品状态", allowableValues = {"ACTIVE", "INACTIVE", "DRAFT"}, example = "ACTIVE")
        @Pattern(regexp = "ACTIVE|INACTIVE|DRAFT")
        String status,
        @Schema(description = "标签 ID 列表", example = "[1,2]")
        List<Long> tagIds,
        @Schema(description = "食材列表")
        List<@Valid IngredientRequest> ingredients
) {
}
