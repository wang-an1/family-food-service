package com.familyfood.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.StatusValues;
import com.familyfood.dish.api.DishCatalogApi;
import com.familyfood.dish.entity.Dish;
import com.familyfood.order.dao.OrderItemMapper;
import com.familyfood.order.dao.OrderStatusLogMapper;
import com.familyfood.order.dao.PersonalOrderMapper;
import com.familyfood.order.dto.DishQuantitySummary;
import com.familyfood.order.dto.OrderItemRequest;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.dto.OrderResponse;
import com.familyfood.order.dto.OrderView;
import com.familyfood.order.entity.MealSession;
import com.familyfood.order.entity.OrderItem;
import com.familyfood.order.entity.OrderStatusLog;
import com.familyfood.order.entity.PersonalOrder;
import com.familyfood.order.service.MealSessionService;
import com.familyfood.order.service.OrderDomainService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderDomainServiceImpl implements OrderDomainService {
    private static final Logger logger = LoggerFactory.getLogger(OrderDomainServiceImpl.class);

    private final PersonalOrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderStatusLogMapper logMapper;
    private final MealSessionService mealSessionService;
    private final DishCatalogApi dishCatalogApi;

    @Autowired
    public OrderDomainServiceImpl(PersonalOrderMapper orderMapper, OrderItemMapper itemMapper,
                              OrderStatusLogMapper logMapper, MealSessionService mealSessionService,
                              DishCatalogApi dishCatalogApi) {
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
        this.logMapper = logMapper;
        this.mealSessionService = mealSessionService;
        this.dishCatalogApi = dishCatalogApi;
    }

    @Transactional
    public OrderResponse submit(ActorContext actor, OrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw AppException.validation("请至少选择一道菜");
        }
        MealSession session = mealSessionService.requireFamilySession(actor, request.mealSessionId());
        if (!"OPEN".equals(session.getStatus())) {
            throw AppException.conflict("餐次未开放，不能下单");
        }
        LocalDateTime now = LocalDateTime.now();
        PersonalOrder order = new PersonalOrder();
        order.setFamilyId(actor.familyId());
        order.setMealSessionId(session.getId());
        order.setUserId(actor.userId());
        order.setStatus(session.getConfirmRequired() == 1 ? "PENDING_CONFIRM" : "CONFIRMED");
        order.setNote(request.note());
        order.setAvoidances(request.avoidances());
        order.setExpectedTime(request.expectedTime());
        order.setSubmittedAt(now);
        order.setConfirmedAt(session.getConfirmRequired() == 1 ? null : now);
        order.setConfirmedBy(session.getConfirmRequired() == 1 ? null : actor.userId());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setDeleted(0);
        orderMapper.insert(order);
        insertItems(actor, order.getId(), request.items());
        log(order.getId(), null, order.getStatus(), actor.userId(), "submit order");
        return getOrder(actor, order.getId());
    }

    @Transactional
    public OrderResponse update(ActorContext actor, Long id, OrderRequest request) {
        PersonalOrder order = ownedOrder(actor, id);
        boolean owner = Objects.equals(order.getUserId(), actor.userId());
        if (!actor.admin() && (!owner || !"PENDING_CONFIRM".equals(order.getStatus()))) {
            throw AppException.forbidden("只能修改自己待确认的订单");
        }
        if (!"PENDING_CONFIRM".equals(order.getStatus()) && !actor.admin()) {
            throw AppException.conflict("当前订单状态不能修改");
        }
        order.setNote(request.note());
        order.setAvoidances(request.avoidances());
        order.setExpectedTime(request.expectedTime());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        itemMapper.delete(new QueryWrapper<OrderItem>().eq("order_id", id));
        insertItems(actor, id, request.items());
        return getOrder(actor, id);
    }

    @Transactional
    public OrderResponse confirm(ActorContext actor, Long id, String note) {
        PersonalOrder order = ownedOrder(actor, id);
        if (!"PENDING_CONFIRM".equals(order.getStatus())) {
            throw AppException.conflict("只有待确认订单可以确认");
        }
        String from = order.getStatus();
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());
        order.setConfirmedBy(actor.userId());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log(order.getId(), from, "CONFIRMED", actor.userId(), note);
        return getOrder(actor, id);
    }

    @Transactional
    public OrderResponse cancel(ActorContext actor, Long id, String reason) {
        PersonalOrder order = ownedOrder(actor, id);
        boolean owner = Objects.equals(order.getUserId(), actor.userId());
        if (!actor.admin() && !owner) {
            throw AppException.forbidden("只能取消自己的订单");
        }
        if (!actor.admin() && !List.of("DRAFT", "SUBMITTED", "PENDING_CONFIRM").contains(order.getStatus())) {
            throw AppException.conflict("当前订单状态不能取消");
        }
        if (List.of("COMPLETED", "CANCELLED").contains(order.getStatus())) {
            throw AppException.conflict("当前订单状态不能取消");
        }
        String from = order.getStatus();
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        log(order.getId(), from, "CANCELLED", actor.userId(), reason);
        return getOrder(actor, id);
    }

    public List<OrderResponse> list(ActorContext actor, Long mealSessionId, String status, Long userId) {
        String normalizedStatus = StatusValues.optional(status, StatusValues.ORDER_STATUSES, "status");
        Long scopedUserId = actor.admin() ? userId : actor.userId();
        return toResponses(orderMapper.selectOrderViews(actor.familyId(), mealSessionId, normalizedStatus, scopedUserId));
    }

    public OrderResponse getOrder(ActorContext actor, Long id) {
        PersonalOrder order = ownedOrder(actor, id);
        OrderView view = orderMapper.selectOrderViewById(order.getId(), actor.familyId());
        if (view == null) {
            throw AppException.notFound("未找到订单");
        }
        return toResponses(List.of(view)).get(0);
    }

    public Map<String, BigDecimal> summaryByDish(ActorContext actor, Long mealSessionId) {
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        for (DishQuantitySummary item : orderMapper.selectDishQuantitySummary(actor.familyId(), mealSessionId)) {
            summary.put(item.getDishNameSnapshot(), item.getAmount());
        }
        return summary;
    }

    private PersonalOrder ownedOrder(ActorContext actor, Long id) {
        PersonalOrder order = orderMapper.selectById(id);
        if (order == null || !Objects.equals(order.getFamilyId(), actor.familyId())
                || Objects.equals(order.getDeleted(), 1)) {
            throw AppException.notFound("未找到订单");
        }
        if (!actor.admin() && !Objects.equals(order.getUserId(), actor.userId())) {
            throw AppException.forbidden("无权限访问该订单");
        }
        return order;
    }

    private void insertItems(ActorContext actor, Long orderId, List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw AppException.validation("请至少选择一道菜");
        }
        Set<Long> dishIds = items.stream()
                .map(OrderItemRequest::dishId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Dish> dishes = dishCatalogApi.requireAvailableDishes(actor, dishIds);
        for (OrderItemRequest request : items) {
            Dish dish = dishes.get(request.dishId());
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setDishId(dish.getId());
            item.setDishNameSnapshot(dish.getName());
            item.setQuantity(request.quantity() == null ? BigDecimal.ONE : request.quantity());
            item.setUnit(request.unit() == null || request.unit().isBlank() ? "item" : request.unit());
            item.setNote(request.note());
            itemMapper.insert(item);
        }
    }

    private List<OrderResponse> toResponses(List<OrderView> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        Set<Long> orderIds = orders.stream().map(PersonalOrder::getId).collect(Collectors.toSet());
        Map<Long, List<OrderItem>> itemsByOrder = itemMapper.selectByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream().map(order -> new OrderResponse(order.getId(), order.getMealSessionId(), order.getUserId(),
                order.getUserNickname(), order.getStatus(), order.getNote(), order.getAvoidances(),
                order.getExpectedTime(), order.getSubmittedAt(), order.getConfirmedAt(), order.getConfirmedBy(),
                itemsByOrder.getOrDefault(order.getId(), List.of()))).toList();
    }

    private void log(Long orderId, String from, String to, Long operatorId, String reason) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorId(operatorId);
        log.setReason(reason);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
        logger.info("order_status_changed orderId={} fromStatus={} toStatus={} operatorId={} reason={}",
                orderId, from, to, operatorId, reason);
    }
}
