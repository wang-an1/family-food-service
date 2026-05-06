package com.familyfood.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "系统配置项更新请求")
public record ConfigItem(
        @Schema(description = "配置键", example = "ai.provider")
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]+")
        String key,
        @Schema(description = "配置值", example = "deepseek")
        @Size(max = 4000)
        String value,
        @Schema(description = "值类型", allowableValues = {"STRING", "NUMBER", "BOOLEAN", "JSON"}, example = "STRING")
        @Size(max = 30)
        String valueType
) {
}
