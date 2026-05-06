package com.familyfood.dish.dto;

import com.familyfood.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "食材请求")
public record IngredientRequest(
        @Schema(description = "食材名称", example = "鸡蛋")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "食材数量", example = "2")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,
        @Schema(description = "单位", example = "个")
        @Size(max = 30)
        String unit,
        @Schema(description = "食材分类", example = "蛋奶")
        @Size(max = 60)
        String category,
        @Schema(description = "是否必需", example = "true")
        Boolean required,
        @Schema(description = "备注", example = "可按人数调整")
        @Size(max = 500)
        String note
) {
}
