package com.familyfood.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "系统配置项响应")
public record ConfigResponse(
        @Schema(description = "配置键", example = "ai.provider")
        String key,
        @Schema(description = "配置值", example = "deepseek")
        String value,
        @Schema(description = "值类型", example = "STRING")
        String valueType,
        @Schema(description = "是否已配置", example = "true")
        boolean configured) {
}
