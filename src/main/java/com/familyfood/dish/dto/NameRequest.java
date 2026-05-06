package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "名称请求")
public record NameRequest(
        @Schema(description = "名称", example = "家常菜")
        @NotBlank @Size(max = 60)
        String name) {
}
