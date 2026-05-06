package com.familyfood.order.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderDomainService {
    OrderResponse submit(ActorContext actor, OrderRequest request);

    OrderResponse update(ActorContext actor, Long id, OrderRequest request);

    OrderResponse confirm(ActorContext actor, Long id, String note);

    OrderResponse cancel(ActorContext actor, Long id, String reason);

    List<OrderResponse> list(ActorContext actor, Long mealSessionId, String status, Long userId);

    OrderResponse getOrder(ActorContext actor, Long id);

    Map<String, BigDecimal> summaryByDish(ActorContext actor, Long mealSessionId);
}
