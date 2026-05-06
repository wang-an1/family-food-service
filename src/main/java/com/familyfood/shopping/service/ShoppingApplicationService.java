package com.familyfood.shopping.service;

import com.familyfood.shopping.dto.ItemRequest;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingListItem;

public interface ShoppingApplicationService {
    ShoppingResponse generate(Long mealSessionId);

    ShoppingResponse get(Long mealSessionId);

    ShoppingListItem updateItem(Long id, ItemRequest request);

    ShoppingListItem check(Long id, boolean checked);
}
