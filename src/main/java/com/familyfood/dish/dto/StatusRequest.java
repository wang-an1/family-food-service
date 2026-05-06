package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "状态更新请求")
public record StatusRequest(
        @Schema(description = "状态", allowableValues = {"ACTIVE", "INACTIVE", "DRAFT"}, example = "ACTIVE")
        @NotBlank
        @Pattern(regexp = "ACTIVE|INACTIVE|DRAFT")
        String status) {
}
