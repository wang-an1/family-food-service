package com.familyfood.dish.api;

import java.math.BigDecimal;
import java.util.List;

public record DishCreateCommand(
        Long categoryId,
        String name,
        String aliases,
        String description,
        String imageUrl,
        String taste,
        List<String> mealTypes,
        String difficulty,
        Integer estimatedMinutes,
        BigDecimal defaultServings,
        String instructions,
        String sourceType,
        String sourceUrl,
        String status,
        List<Ingredient> ingredients
) {
    public record Ingredient(
            String name,
            BigDecimal amount,
            String unit,
            String category,
            Boolean required,
            String note
    ) {
    }
}
