package com.familyfood.order.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.dto.NoteRequest;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.dto.OrderResponse;
import com.familyfood.order.entity.MealSession;
import com.familyfood.order.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "点餐订单", description = "餐次、个人订单和订单汇总")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderApplicationService orderService;

    @Autowired
    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/meal-sessions")
    @Operation(summary = "查询餐次列表", description = "返回当前家庭的餐次列表")
    public ApiResponse<List<MealSession>> sessions() {
        return ApiResponse.ok(orderService.sessions());
    }

    @PostMapping("/meal-sessions")
    @Operation(summary = "创建餐次", description = "创建早餐、午餐、晚餐等点餐餐次")
    public ApiResponse<MealSession> createSession(@Valid @RequestBody MealSessionRequest request) {
        return ApiResponse.ok(orderService.createSession(request));
    }

    @GetMapping("/meal-sessions/current")
    @Operation(summary = "获取当前餐次", description = "返回当前可点餐的餐次")
    public ApiResponse<MealSession> current() {
        return ApiResponse.ok(orderService.current());
    }

    @PostMapping("/orders")
    @Operation(summary = "提交订单", description = "提交当前用户在指定餐次下的点餐订单")
    public ApiResponse<OrderResponse> submit(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.submit(request));
    }

    @PutMapping("/orders/{id}")
    @Operation(summary = "更新订单", description = "更新指定订单的菜品、备注、忌口和期望时间")
    public ApiResponse<OrderResponse> update(@Parameter(description = "订单 ID", required = true) @PathVariable Long id,
                                             @Valid @RequestBody OrderRequest request) {
        return ApiResponse.ok(orderService.update(id, request));
    }

    @GetMapping("/orders")
    @Operation(summary = "查询订单列表", description = "按餐次、状态或用户筛选订单")
    public ApiResponse<List<OrderResponse>> list(
            @Parameter(description = "餐次 ID") @RequestParam(required = false) Long mealSessionId,
            @Parameter(description = "订单状态", example = "SUBMITTED") @RequestParam(required = false) String status,
            @Parameter(description = "用户 ID") @RequestParam(required = false) Long userId) {
        return ApiResponse.ok(orderService.list(mealSessionId, status, userId));
    }

    @PostMapping("/orders/{id}/confirm")
    @Operation(summary = "确认订单", description = "管理员确认指定订单")
    public ApiResponse<OrderResponse> confirm(@Parameter(description = "订单 ID", required = true) @PathVariable Long id,
                                              @Valid @RequestBody(required = false) NoteRequest request) {
        return ApiResponse.ok(orderService.confirm(id, request == null ? "已确认" : request.note()));
    }

    @PostMapping("/orders/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定订单并记录原因")
    public ApiResponse<OrderResponse> cancel(@Parameter(description = "订单 ID", required = true) @PathVariable Long id,
                                             @Valid @RequestBody(required = false) NoteRequest request) {
        return ApiResponse.ok(orderService.cancel(id, request == null ? "取消订单" : request.reason()));
    }

    @GetMapping("/orders/summary")
    @Operation(summary = "查询订单汇总", description = "按菜品汇总指定餐次下的订单数量")
    public ApiResponse<Map<String, BigDecimal>> summary(
            @Parameter(description = "餐次 ID", required = true) @RequestParam Long mealSessionId) {
        return ApiResponse.ok(orderService.summaryByDish(mealSessionId));
    }
}
