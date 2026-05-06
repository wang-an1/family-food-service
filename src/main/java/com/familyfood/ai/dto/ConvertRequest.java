package com.familyfood.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

@Schema(description = "AI 菜品草稿转换请求")
public record ConvertRequest(
        @Schema(description = "转换模式", allowableValues = {"CREATE", "UPDATE"}, example = "CREATE")
        @Size(max = 30)
        String mode,
        @Schema(description = "更新已有菜品时的目标菜品 ID", example = "1")
        Long targetDishId,
        @Schema(description = "转换时覆盖 AI 草稿的字段")
        @Valid
        ConvertOverride override
) {
}
