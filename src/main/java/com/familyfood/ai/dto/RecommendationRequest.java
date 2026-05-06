package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 菜品推荐请求")
public record RecommendationRequest(
        @Schema(description = "推荐要求", example = "晚餐想吃清淡一点，有鸡蛋和番茄")
        @NotBlank @Size(max = 2000)
        String prompt,
        @Schema(description = "餐次类型", allowableValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"}, example = "DINNER")
        @Pattern(regexp = "BREAKFAST|LUNCH|DINNER|SNACK|CUSTOM")
        String mealType,
        @Schema(description = "最大推荐数量", example = "5")
        @Min(1) @Max(20)
        Integer maxResults
) {
}
