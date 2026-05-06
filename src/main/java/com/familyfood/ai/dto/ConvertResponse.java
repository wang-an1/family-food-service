package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 菜品草稿转换响应")
public record ConvertResponse(
        @Schema(description = "转换后的正式菜品 ID", example = "1")
        Long dishId,
        @Schema(description = "AI 草稿审核状态", example = "CONVERTED")
        String reviewStatus) {
}
