package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 模型目录响应")
public record AiModelCatalogResponse(
        @Schema(description = "模型 ID", example = "1")
        Long id,
        @Schema(description = "供应商 ID", example = "1")
        Long providerId,
        @Schema(description = "模型名称", example = "deepseek-v4-pro")
        String modelName,
        @Schema(description = "展示名称", example = "DeepSeek V4 Pro")
        String displayName,
        @Schema(description = "是否默认模型")
        boolean defaultModel,
        @Schema(description = "状态", example = "ACTIVE")
        String status,
        @Schema(description = "排序", example = "10")
        Integer sortOrder
) {
}
