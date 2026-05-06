package com.familyfood.shopping.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.shopping.dto.ItemRequest;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingListItem;

public interface ShoppingListService {
    ShoppingResponse get(ActorContext actor, Long mealSessionId);

    ShoppingResponse generate(ActorContext actor, Long mealSessionId);

    ShoppingListItem updateItem(ActorContext actor, Long id, ItemRequest request);

    ShoppingListItem check(ActorContext actor, Long id, boolean checked);
}
