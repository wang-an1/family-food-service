package com.familyfood.order.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.dto.OrderResponse;
import com.familyfood.order.entity.MealSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderApplicationService {
    private final ActorContextProvider actorProvider;
    private final MealSessionService mealSessionService;
    private final OrderDomainService orderDomainService;

    public OrderApplicationService(ActorContextProvider actorProvider, MealSessionService mealSessionService,
                                   OrderDomainService orderDomainService) {
        this.actorProvider = actorProvider;
        this.mealSessionService = mealSessionService;
        this.orderDomainService = orderDomainService;
    }

    public List<MealSession> sessions() {
        return mealSessionService.sessions(actorProvider.current());
    }

    public MealSession current() {
        return mealSessionService.current(actorProvider.current());
    }

    @Transactional
    public MealSession createSession(MealSessionRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return mealSessionService.create(actor, request);
    }

    @Transactional
    public OrderResponse submit(OrderRequest request) {
        return orderDomainService.submit(actorProvider.current(), request);
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        return orderDomainService.update(actorProvider.current(), id, request);
    }

    @Transactional
    public OrderResponse confirm(Long id, String note) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return orderDomainService.confirm(actor, id, note);
    }

    @Transactional
    public OrderResponse cancel(Long id, String reason) {
        return orderDomainService.cancel(actorProvider.current(), id, reason);
    }

    public List<OrderResponse> list(Long mealSessionId, String status, Long userId) {
        return orderDomainService.list(actorProvider.current(), mealSessionId, status, userId);
    }

    public OrderResponse getOrder(Long id) {
        return orderDomainService.getOrder(actorProvider.current(), id);
    }

    public Map<String, BigDecimal> summaryByDish(Long mealSessionId) {
        return orderDomainService.summaryByDish(actorProvider.current(), mealSessionId);
    }
}
