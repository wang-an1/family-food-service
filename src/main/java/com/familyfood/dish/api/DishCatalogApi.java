package com.familyfood.dish.api;

import com.familyfood.common.context.ActorContext;
import com.familyfood.dish.entity.Dish;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DishCatalogApi {
    Map<Long, Dish> requireAvailableDishes(ActorContext actor, Collection<Long> dishIds);

    List<DishCandidate> activeCandidates(ActorContext actor, int limit);

    List<String> existingDishNames(Long familyId, int limit);

    Dish createDish(ActorContext actor, DishCreateCommand command);

    Long findCategoryId(Long familyId, String categoryName, String fallbackName);
}
