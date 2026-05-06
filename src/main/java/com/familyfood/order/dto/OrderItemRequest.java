package com.familyfood.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "订单菜品请求")
public record OrderItemRequest(
        @Schema(description = "菜品 ID", example = "1")
        @NotNull
        Long dishId,
        @Schema(description = "数量", example = "1")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal quantity,
        @Schema(description = "单位", example = "份")
        @Size(max = 30)
        String unit,
        @Schema(description = "单项备注", example = "少盐")
        @Size(max = 500)
        String note
) {
}
