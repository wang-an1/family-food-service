package com.familyfood.dish.service;

import com.familyfood.common.context.ActorContext;
import com.familyfood.dish.api.DishCandidate;
import com.familyfood.dish.api.DishCatalogApi;
import com.familyfood.dish.api.DishCreateCommand;
import com.familyfood.dish.dto.DishRequest;
import com.familyfood.dish.dto.DishResponse;
import com.familyfood.dish.dto.TagRequest;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishTag;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DishCatalogService extends DishCatalogApi {
    List<DishResponse> list(ActorContext actor, String keyword, Long categoryId, Long tagId, String status);

    DishResponse get(ActorContext actor, Long id);

    DishResponse create(ActorContext actor, DishRequest request);

    DishResponse update(ActorContext actor, Long id, DishRequest request);

    DishResponse updateStatus(ActorContext actor, Long id, String status);

    void delete(ActorContext actor, Long id);

    List<DishCategory> categories(ActorContext actor);

    DishCategory createCategory(ActorContext actor, String name);

    List<DishTag> tags(ActorContext actor);

    DishTag createTag(ActorContext actor, TagRequest request);

    Map<Long, Dish> requireAvailableDishes(ActorContext actor, Collection<Long> dishIds);

    List<DishCandidate> activeCandidates(ActorContext actor, int limit);

    List<String> existingDishNames(Long familyId, int limit);

    Dish createDish(ActorContext actor, DishCreateCommand command);

    Long findCategoryId(Long familyId, String categoryName, String fallbackName);
}
