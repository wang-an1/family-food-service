package com.familyfood.ai.provider;

import com.familyfood.ai.dto.RecommendationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public interface AiProvider {
    default String modelName() {
        return getClass().getSimpleName();
    }

    AiStructuredResult extractDishes(AiExtractionRequest request);

    AiRecommendationResult recommend(AiRecommendationRequest request);

    AiMenuPlanResult planMenu(AiMenuPlanRequest request);

    String summarize(AiSummarizeRequest request);

    record AiExtractionRequest(String sourceType, String title, String description, String contentText,
                               String fallbackText, List<String> existingDishNames) {
    }

    record AiStructuredResult(String summary, List<ExtractedDishDto> dishes) {
    }

    record ExtractedDishDto(String name, List<String> tags, String taste, List<String> mealTypes,
                            String difficulty, Integer estimatedMinutes, List<IngredientDto> ingredients,
                            String instructions, String reason, double confidence) {
    }

    record IngredientDto(String name, double amount, String unit, String category, boolean required) {
    }

    record AiRecommendationRequest(String prompt, String mealType, int maxResults, List<CandidateDish> candidateDishes) {
    }

    record CandidateDish(Long dishId, String name, List<String> tags, String taste, List<String> mealTypes) {
    }

    record AiRecommendationResult(List<RecommendationDto> recommendations, String fallbackMessage) {
    }

    record RecommendationDto(String type, Long dishId, String title, String reason, double score) {
    }

    record AiMenuPlanRequest(String prompt, String mealType, int peopleCount, String avoidances, List<CandidateDish> candidates) {
    }

    @Schema(description = "AI 菜单计划响应")
    record AiMenuPlanResult(
            @Schema(description = "菜单标题", example = "清淡晚餐组合")
            String planTitle,
            @Schema(description = "菜单项列表")
            List<MenuPlanItem> items,
            @Schema(description = "采购摘要")
            String shoppingSummary) {
    }

    @Schema(description = "AI 菜单计划项")
    record MenuPlanItem(
            @Schema(description = "菜品 ID", example = "1")
            Long dishId,
            @Schema(description = "菜品名称", example = "番茄炒蛋")
            String name,
            @Schema(description = "菜单角色", example = "主菜")
            String role,
            @Schema(description = "推荐理由")
            String reason) {
    }

    record AiSummarizeRequest(String title, String contentText) {
    }
}
