package com.familyfood.dish.api;

import java.util.List;

public record DishCandidate(
        Long id,
        String name,
        List<String> tags,
        String taste,
        List<String> mealTypes
) {
}
