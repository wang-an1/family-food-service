package com.familyfood.order.dto;

import com.familyfood.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单详情响应")
public record OrderResponse(
        @Schema(description = "订单 ID", example = "1")
        Long id,
        @Schema(description = "餐次 ID", example = "1")
        Long mealSessionId,
        @Schema(description = "下单用户 ID", example = "2")
        Long userId,
        @Schema(description = "下单用户昵称", example = "成员")
        String userNickname,
        @Schema(description = "订单状态", example = "SUBMITTED")
        String status,
        @Schema(description = "订单备注")
        String note,
        @Schema(description = "忌口说明")
        String avoidances,
        @Schema(description = "期望时间", format = "date-time")
        LocalDateTime expectedTime,
        @Schema(description = "提交时间", format = "date-time")
        LocalDateTime submittedAt,
        @Schema(description = "确认时间", format = "date-time")
        LocalDateTime confirmedAt,
        @Schema(description = "确认人用户 ID", example = "1")
        Long confirmedBy,
        @Schema(description = "订单菜品列表")
        List<OrderItem> items) {
}
