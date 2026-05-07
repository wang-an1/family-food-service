package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 供应商目录响应")
public record AiProviderCatalogResponse(
        @Schema(description = "供应商 ID", example = "1")
        Long id,
        @Schema(description = "供应商编码", example = "deepseek")
        String code,
        @Schema(description = "展示名称", example = "DeepSeek")
        String displayName,
        @Schema(description = "调用类型", example = "OPENAI_CHAT_COMPLETIONS")
        String callType,
        @Schema(description = "API 基础地址", example = "https://api.deepseek.com")
        String baseUrl,
        @Schema(description = "状态", example = "ACTIVE")
        String status,
        @Schema(description = "排序", example = "10")
        Integer sortOrder,
        @Schema(description = "模型列表")
        List<AiModelCatalogResponse> models
) {
}
