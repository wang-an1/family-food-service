package com.familyfood.ai.dto;

import com.familyfood.ai.entity.AiExtractedDish;
import com.familyfood.ai.entity.AiRecommendation;
import com.familyfood.ai.entity.AiSourceContent;
import com.familyfood.ai.entity.AiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 任务详情")
public record AiTaskDetail(
        @Schema(description = "AI 任务信息")
        AiTask task,
        @Schema(description = "来源内容")
        AiSourceContent sourceContent,
        @Schema(description = "AI 抽取的菜品草稿")
        List<AiExtractedDish> extractedDishes,
        @Schema(description = "AI 推荐结果")
        List<AiRecommendation> recommendations) {
}
