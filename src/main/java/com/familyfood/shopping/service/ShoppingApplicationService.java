package com.familyfood.shopping.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.shopping.dto.ItemRequest;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingListItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShoppingApplicationService {
    private final ActorContextProvider actorProvider;
    private final ShoppingListService shoppingListService;

    public ShoppingApplicationService(ActorContextProvider actorProvider, ShoppingListService shoppingListService) {
        this.actorProvider = actorProvider;
        this.shoppingListService = shoppingListService;
    }

    @Transactional
    public ShoppingResponse generate(Long mealSessionId) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return shoppingListService.generate(actor, mealSessionId);
    }

    public ShoppingResponse get(Long mealSessionId) {
        return shoppingListService.get(actorProvider.current(), mealSessionId);
    }

    @Transactional
    public ShoppingListItem updateItem(Long id, ItemRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return shoppingListService.updateItem(actor, id, request);
    }

    @Transactional
    public ShoppingListItem check(Long id, boolean checked) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return shoppingListService.check(actor, id, checked);
    }
}
