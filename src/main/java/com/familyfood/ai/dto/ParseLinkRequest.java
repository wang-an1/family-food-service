package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 链接或素材解析请求")
public record ParseLinkRequest(
        @Schema(description = "待解析链接", example = "https://example.com/recipe")
        @Size(max = 1000)
        String url,
        @Schema(description = "链接不可用时的兜底文本")
        @Size(max = 5000)
        String fallbackText,
        @Schema(description = "待解析图片 URL", example = "/uploads/ai/menu.jpg")
        @Size(max = 1000)
        String imageUrl
) {
}
