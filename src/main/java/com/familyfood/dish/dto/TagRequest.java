package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "菜品标签请求")
public record TagRequest(
        @Schema(description = "标签名称", example = "少油")
        @NotBlank @Size(max = 60)
        String name,
        @Schema(description = "标签颜色", example = "#22c55e")
        @Size(max = 30)
        String color
) {
}
