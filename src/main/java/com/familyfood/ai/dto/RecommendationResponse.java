package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 菜品推荐响应")
public record RecommendationResponse(
        @Schema(description = "AI 任务 ID", example = "1")
        Long taskId,
        @Schema(description = "推荐项列表")
        List<RecommendationDto> recommendations) {
}
