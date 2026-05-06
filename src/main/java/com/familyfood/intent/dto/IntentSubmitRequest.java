package com.familyfood.intent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "点餐意图提交请求")
public record IntentSubmitRequest(
        @Schema(description = "来源类型", allowableValues = {"TEXT", "LINK", "IMAGE"}, example = "TEXT")
        @Size(max = 30)
        String sourceType,
        @Schema(description = "输入文本", example = "今晚想吃番茄炒蛋")
        @Size(max = 5000)
        String inputText,
        @Schema(description = "来源链接", example = "https://example.com/recipe")
        @Size(max = 1000)
        String sourceUrl,
        @Schema(description = "图片 URL", example = "/uploads/intent/menu.jpg")
        @Size(max = 1000)
        String imageUrl,
        @Schema(description = "备注", example = "少油")
        @Size(max = 500)
        String note
) {
}
