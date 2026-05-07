package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 模型维护请求")
public record AiModelRequest(
        @Schema(description = "模型名称", example = "deepseek-v4-pro")
        @NotBlank @Size(max = 128)
        String modelName,
        @Schema(description = "展示名称", example = "DeepSeek V4 Pro")
        @NotBlank @Size(max = 128)
        String displayName,
        @Schema(description = "是否默认模型")
        Boolean defaultModel,
        @Schema(description = "状态", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE")
        String status,
        @Schema(description = "排序", example = "10")
        Integer sortOrder
) {
}
