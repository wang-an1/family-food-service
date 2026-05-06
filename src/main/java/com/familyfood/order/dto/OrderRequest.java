package com.familyfood.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单保存请求")
public record OrderRequest(
        @Schema(description = "餐次 ID", example = "1")
        @NotNull
        Long mealSessionId,
        @Schema(description = "订单备注", example = "米饭少一点")
        @Size(max = 500)
        String note,
        @Schema(description = "忌口说明", example = "不吃香菜")
        @Size(max = 500)
        String avoidances,
        @Schema(description = "期望取餐或就餐时间", format = "date-time", example = "2026-04-29T18:30:00")
        LocalDateTime expectedTime,
        @Schema(description = "订单菜品列表")
        @NotEmpty
        List<@Valid OrderItemRequest> items
) {
}
