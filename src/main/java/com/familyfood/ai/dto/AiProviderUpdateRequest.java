package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 供应商更新请求")
public record AiProviderUpdateRequest(
        @Schema(description = "展示名称", example = "DeepSeek")
        @NotBlank @Size(max = 128)
        String displayName,
        @Schema(description = "调用类型", allowableValues = {"OPENAI_CHAT_COMPLETIONS", "MOCK"}, example = "OPENAI_CHAT_COMPLETIONS")
        @NotBlank @Pattern(regexp = "OPENAI_CHAT_COMPLETIONS|MOCK")
        String callType,
        @Schema(description = "API 基础地址", example = "https://api.deepseek.com")
        @Size(max = 500)
        String baseUrl,
        @Schema(description = "状态", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE")
        String status,
        @Schema(description = "排序", example = "10")
        Integer sortOrder
) {
}
