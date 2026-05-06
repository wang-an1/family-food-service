package com.familyfood.shopping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.order.api.MealSessionApi;
import com.familyfood.order.entity.MealSession;
import com.familyfood.shopping.dao.ShoppingListItemMapper;
import com.familyfood.shopping.dao.ShoppingListMapper;
import com.familyfood.shopping.dto.ItemRequest;
import com.familyfood.shopping.dto.ShoppingIngredientSummary;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingList;
import com.familyfood.shopping.entity.ShoppingListItem;
import com.familyfood.shopping.service.ShoppingListService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShoppingListServiceImpl implements ShoppingListService {
    private final ShoppingListMapper listMapper;
    private final ShoppingListItemMapper itemMapper;
    private final MealSessionApi mealSessionApi;

    @Autowired
    public ShoppingListServiceImpl(ShoppingListMapper listMapper, ShoppingListItemMapper itemMapper,
                               MealSessionApi mealSessionApi) {
        this.listMapper = listMapper;
        this.itemMapper = itemMapper;
        this.mealSessionApi = mealSessionApi;
    }

    public ShoppingResponse get(ActorContext actor, Long mealSessionId) {
        ShoppingList list = listMapper.selectOne(new QueryWrapper<ShoppingList>()
                .eq("family_id", actor.familyId())
                .eq("meal_session_id", mealSessionId));
        if (list == null) {
            return new ShoppingResponse(null, mealSessionId, "Not generated", List.of());
        }
        List<ShoppingListItem> items = itemMapper.selectList(new QueryWrapper<ShoppingListItem>()
                .eq("shopping_list_id", list.getId())
                .orderByAsc("checked", "category", "name"));
        return new ShoppingResponse(list.getId(), mealSessionId, list.getTitle(), items);
    }

    @Transactional
    public ShoppingResponse generate(ActorContext actor, Long mealSessionId) {
        MealSession session = mealSessionApi.requireFamilySession(actor, mealSessionId);
        LocalDateTime now = LocalDateTime.now();
        ShoppingList list = listMapper.selectOne(new QueryWrapper<ShoppingList>()
                .eq("family_id", actor.familyId()).eq("meal_session_id", mealSessionId));
        if (list == null) {
            list = new ShoppingList();
            list.setFamilyId(actor.familyId());
            list.setMealSessionId(mealSessionId);
            list.setTitle(session.getTitle() + " shopping list");
            list.setStatus("OPEN");
            list.setGeneratedByAi(0);
            list.setCreatedAt(now);
            list.setUpdatedAt(now);
            listMapper.insert(list);
        } else {
            list.setUpdatedAt(now);
            listMapper.updateById(list);
        }

        Map<String, ShoppingListItem> existing = itemMapper.selectList(new QueryWrapper<ShoppingListItem>()
                        .eq("shopping_list_id", list.getId()))
                .stream().collect(Collectors.toMap(i -> key(i.getName(), i.getUnit()), i -> i, (a, b) -> a));
        itemMapper.delete(new QueryWrapper<ShoppingListItem>().eq("shopping_list_id", list.getId()).eq("source", "DISH"));

        for (ShoppingIngredientSummary summary : itemMapper.selectDishIngredientSummaries(actor.familyId(), mealSessionId)) {
            ShoppingListItem carried = existing.get(key(summary.getName(), summary.getUnit()));
            ShoppingListItem item = new ShoppingListItem();
            item.setShoppingListId(list.getId());
            item.setName(summary.getName());
            item.setAmount(summary.getAmount());
            item.setUnit(summary.getUnit());
            item.setCategory(summary.getCategory());
            item.setChecked(carried == null ? 0 : carried.getChecked());
            item.setSource("DISH");
            item.setSourceDishIds(summary.getSourceDishIds());
            item.setNote(carried == null ? null : carried.getNote());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            itemMapper.insert(item);
        }
        return get(actor, mealSessionId);
    }

    @Transactional
    public ShoppingListItem updateItem(ActorContext actor, Long id, ItemRequest request) {
        ShoppingListItem item = itemMapper.selectById(id);
        ensureItemAccess(actor, item);
        item.setName(request.name());
        item.setAmount(request.amount());
        item.setUnit(request.unit());
        item.setCategory(request.category());
        item.setNote(request.note());
        item.setSource("MANUAL");
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    @Transactional
    public ShoppingListItem check(ActorContext actor, Long id, boolean checked) {
        ShoppingListItem item = itemMapper.selectById(id);
        ensureItemAccess(actor, item);
        item.setChecked(checked ? 1 : 0);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
        return item;
    }

    private void ensureItemAccess(ActorContext actor, ShoppingListItem item) {
        if (item == null) {
            throw AppException.notFound("未找到采购条目");
        }
        ShoppingList list = listMapper.selectById(item.getShoppingListId());
        if (list == null || !Objects.equals(list.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到采购条目");
        }
    }

    private String key(String name, String unit) {
        return (name == null ? "" : name.trim()) + "::" + (unit == null ? "" : unit.trim());
    }
}
