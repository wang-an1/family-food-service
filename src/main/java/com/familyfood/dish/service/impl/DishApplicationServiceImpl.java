package com.familyfood.dish.service.impl;

import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.dish.dto.DishRequest;
import com.familyfood.dish.dto.DishResponse;
import com.familyfood.dish.dto.TagRequest;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishTag;
import com.familyfood.dish.service.DishApplicationService;
import com.familyfood.dish.service.DishCatalogService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DishApplicationServiceImpl implements DishApplicationService {
    private final ActorContextProvider actorProvider;
    private final DishCatalogService catalogService;

    @Autowired
    public DishApplicationServiceImpl(ActorContextProvider actorProvider, DishCatalogService catalogService) {
        this.actorProvider = actorProvider;
        this.catalogService = catalogService;
    }

    public List<DishResponse> list(String keyword, Long categoryId, Long tagId, String status) {
        return catalogService.list(actorProvider.current(), keyword, categoryId, tagId, status);
    }

    public DishResponse get(Long id) {
        return catalogService.get(actorProvider.current(), id);
    }

    @Transactional
    public DishResponse create(DishRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return catalogService.create(actor, request);
    }

    @Transactional
    public DishResponse update(Long id, DishRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return catalogService.update(actor, id, request);
    }

    @Transactional
    public DishResponse updateStatus(Long id, String status) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return catalogService.updateStatus(actor, id, status);
    }

    @Transactional
    public void delete(Long id) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        catalogService.delete(actor, id);
    }

    public List<DishCategory> categories() {
        return catalogService.categories(actorProvider.current());
    }

    @Transactional
    public DishCategory createCategory(String name) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return catalogService.createCategory(actor, name);
    }

    public List<DishTag> tags() {
        return catalogService.tags(actorProvider.current());
    }

    @Transactional
    public DishTag createTag(TagRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return catalogService.createTag(actor, request);
    }
}
