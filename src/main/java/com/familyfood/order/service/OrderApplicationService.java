package com.familyfood.order.service;

import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.dto.OrderResponse;
import com.familyfood.order.entity.MealSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderApplicationService {
    List<MealSession> sessions();

    MealSession current();

    MealSession createSession(MealSessionRequest request);

    OrderResponse submit(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    OrderResponse confirm(Long id, String note);

    OrderResponse cancel(Long id, String reason);

    List<OrderResponse> list(Long mealSessionId, String status, Long userId);

    OrderResponse getOrder(Long id);

    Map<String, BigDecimal> summaryByDish(Long mealSessionId);
}
