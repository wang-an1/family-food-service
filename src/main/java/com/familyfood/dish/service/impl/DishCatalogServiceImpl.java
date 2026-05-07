package com.familyfood.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.StatusValues;
import com.familyfood.dish.api.DishCandidate;
import com.familyfood.dish.api.DishCreateCommand;
import com.familyfood.dish.dao.DishCategoryMapper;
import com.familyfood.dish.dao.DishIngredientMapper;
import com.familyfood.dish.dao.DishMapper;
import com.familyfood.dish.dao.DishTagMapper;
import com.familyfood.dish.dao.DishTagRelationMapper;
import com.familyfood.dish.dto.DishRequest;
import com.familyfood.dish.dto.DishResponse;
import com.familyfood.dish.dto.DishTagView;
import com.familyfood.dish.dto.DishView;
import com.familyfood.dish.dto.TagRequest;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishIngredient;
import com.familyfood.dish.entity.DishTag;
import com.familyfood.dish.entity.DishTagRelation;
import com.familyfood.dish.service.DishCatalogService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DishCatalogServiceImpl implements DishCatalogService {
    private final DishMapper dishMapper;
    private final DishCategoryMapper categoryMapper;
    private final DishTagMapper tagMapper;
    private final DishTagRelationMapper relationMapper;
    private final DishIngredientMapper ingredientMapper;

    @Autowired
    public DishCatalogServiceImpl(DishMapper dishMapper, DishCategoryMapper categoryMapper, DishTagMapper tagMapper,
                              DishTagRelationMapper relationMapper, DishIngredientMapper ingredientMapper) {
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.relationMapper = relationMapper;
        this.ingredientMapper = ingredientMapper;
    }

    public List<DishResponse> list(ActorContext actor, String keyword, Long categoryId, Long tagId, String status) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedStatus = StatusValues.optional(status, StatusValues.DISH_STATUSES, "status");
        List<DishView> dishes = dishMapper.selectDishViews(actor.familyId(), normalizedKeyword, categoryId, tagId,
                normalizedStatus, !actor.admin());
        return enrich(dishes);
    }

    public DishResponse get(ActorContext actor, Long id) {
        DishView dish = dishMapper.selectDishViewById(id, actor.familyId(), !actor.admin());
        if (dish == null) {
            throw AppException.notFound("未找到这个菜品，请刷新后再试");
        }
        return enrich(List.of(dish)).get(0);
    }

    @Transactional
    public DishResponse create(ActorContext actor, DishRequest request) {
        Dish dish = createDish(actor, commandFromRequest(request, request.sourceType() == null ? "MANUAL" : request.sourceType()));
        replaceChildren(dish.getId(), request);
        return get(actor, dish.getId());
    }

    @Transactional
    public DishResponse update(ActorContext actor, Long id, DishRequest request) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !Objects.equals(dish.getFamilyId(), actor.familyId())
                || Objects.equals(dish.getDeleted(), 1)) {
            throw AppException.notFound("未找到这个菜品，请刷新后再试");
        }
        fillDish(dish, commandFromRequest(request, dish.getSourceType()));
        dish.setUpdatedAt(LocalDateTime.now());
        dish.setUpdatedBy(actor.userId());
        dishMapper.updateById(dish);
        replaceChildren(id, request);
        return get(actor, id);
    }

    @Transactional
    public DishResponse updateStatus(ActorContext actor, Long id, String status) {
        String normalizedStatus = StatusValues.required(status, StatusValues.DISH_STATUSES, "status");
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !Objects.equals(dish.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到这个菜品，请刷新后再试");
        }
        dish.setStatus(normalizedStatus);
        dish.setUpdatedAt(LocalDateTime.now());
        dish.setUpdatedBy(actor.userId());
        dishMapper.updateById(dish);
        return get(actor, id);
    }

    @Transactional
    public void delete(ActorContext actor, Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null || !Objects.equals(dish.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到这个菜品，请刷新后再试");
        }
        dish.setDeleted(1);
        dish.setUpdatedAt(LocalDateTime.now());
        dishMapper.updateById(dish);
    }

    public List<DishCategory> categories(ActorContext actor) {
        return categoryMapper.selectList(new QueryWrapper<DishCategory>()
                .eq("family_id", actor.familyId())
                .orderByAsc("sort_order", "id"));
    }

    @Transactional
    public DishCategory createCategory(ActorContext actor, String name) {
        DishCategory category = new DishCategory();
        category.setFamilyId(actor.familyId());
        category.setName(name);
        category.setSortOrder(100);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    public List<DishTag> tags(ActorContext actor) {
        return tagMapper.selectList(new QueryWrapper<DishTag>()
                .eq("family_id", actor.familyId())
                .orderByAsc("id"));
    }

    @Transactional
    public DishTag createTag(ActorContext actor, TagRequest request) {
        DishTag tag = new DishTag();
        tag.setFamilyId(actor.familyId());
        tag.setName(request.name());
        tag.setColor(request.color());
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Map<Long, Dish> requireAvailableDishes(ActorContext actor, Collection<Long> dishIds) {
        Set<Long> ids = dishIds == null ? Set.of() : dishIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Dish> dishes = dishMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        for (Long id : ids) {
            Dish dish = dishes.get(id);
            if (dish == null || !Objects.equals(dish.getFamilyId(), actor.familyId())
                    || Objects.equals(dish.getDeleted(), 1) || !"ACTIVE".equals(dish.getStatus())) {
                throw AppException.validation("菜品当前不可点餐，请重新选择，菜品 ID：" + id);
            }
        }
        return dishes;
    }

    @Override
    public List<DishCandidate> activeCandidates(ActorContext actor, int limit) {
        int actualLimit = Math.max(1, limit);
        return dishMapper.selectList(new QueryWrapper<Dish>()
                        .eq("family_id", actor.familyId())
                        .eq("status", "ACTIVE")
                        .eq("deleted", 0)
                        .last("limit " + actualLimit))
                .stream()
                .map(d -> new DishCandidate(d.getId(), d.getName(), List.of(), d.getTaste(), split(d.getMealTypes())))
                .toList();
    }

    @Override
    public List<String> existingDishNames(Long familyId, int limit) {
        int actualLimit = Math.max(1, limit);
        return dishMapper.selectList(new QueryWrapper<Dish>()
                        .eq("family_id", familyId)
                        .eq("deleted", 0)
                        .last("limit " + actualLimit))
                .stream()
                .map(Dish::getName)
                .toList();
    }

    @Override
    @Transactional
    public Dish createDish(ActorContext actor, DishCreateCommand command) {
        LocalDateTime now = LocalDateTime.now();
        Dish dish = new Dish();
        fillDish(dish, command);
        dish.setFamilyId(actor.familyId());
        dish.setCreatedAt(now);
        dish.setUpdatedAt(now);
        dish.setCreatedBy(actor.userId());
        dish.setUpdatedBy(actor.userId());
        dish.setDeleted(0);
        dishMapper.insert(dish);
        replaceIngredients(dish.getId(), command.ingredients());
        return dish;
    }

    @Override
    public Long findCategoryId(Long familyId, String categoryName, String fallbackName) {
        String name = categoryName == null || categoryName.isBlank() ? fallbackName : categoryName;
        if (name == null || name.isBlank()) {
            return null;
        }
        DishCategory category = categoryMapper.selectOne(new QueryWrapper<DishCategory>()
                .eq("family_id", familyId)
                .eq("name", name)
                .last("limit 1"));
        return category == null ? null : category.getId();
    }

    private DishCreateCommand commandFromRequest(DishRequest request, String sourceType) {
        return new DishCreateCommand(
                request.categoryId(),
                request.name(),
                request.aliases(),
                request.description(),
                request.imageUrl(),
                request.taste(),
                Optional.ofNullable(request.mealTypes()).orElse(List.of()),
                request.difficulty(),
                request.estimatedMinutes(),
                request.defaultServings(),
                request.instructions(),
                sourceType,
                request.sourceUrl(),
                request.status(),
                Optional.ofNullable(request.ingredients()).orElse(List.of()).stream()
                        .map(i -> new DishCreateCommand.Ingredient(
                                i.name(), i.amount(), i.unit(), i.category(), i.required(), i.note()))
                        .toList()
        );
    }

    private void fillDish(Dish dish, DishCreateCommand command) {
        String status = StatusValues.orDefault(command.status(), "ACTIVE", StatusValues.DISH_STATUSES, "status");
        dish.setCategoryId(command.categoryId());
        dish.setName(command.name());
        dish.setAliases(command.aliases());
        dish.setDescription(command.description());
        dish.setImageUrl(command.imageUrl());
        dish.setTaste(command.taste());
        dish.setMealTypes(String.join(",", Optional.ofNullable(command.mealTypes()).orElse(List.of())));
        dish.setDifficulty(command.difficulty());
        dish.setEstimatedMinutes(command.estimatedMinutes());
        dish.setDefaultServings(command.defaultServings() == null ? BigDecimal.ONE : command.defaultServings());
        dish.setInstructions(command.instructions());
        dish.setSourceType(command.sourceType() == null ? "MANUAL" : command.sourceType());
        dish.setSourceUrl(command.sourceUrl());
        dish.setStatus(status);
    }

    private void replaceChildren(Long dishId, DishRequest request) {
        relationMapper.delete(new QueryWrapper<DishTagRelation>().eq("dish_id", dishId));
        for (Long tagId : Optional.ofNullable(request.tagIds()).orElse(List.of())) {
            DishTagRelation relation = new DishTagRelation();
            relation.setDishId(dishId);
            relation.setTagId(tagId);
            relationMapper.insert(relation);
        }
        replaceIngredients(dishId, Optional.ofNullable(request.ingredients()).orElse(List.of()).stream()
                .map(i -> new DishCreateCommand.Ingredient(
                        i.name(), i.amount(), i.unit(), i.category(), i.required(), i.note()))
                .toList());
    }

    private void replaceIngredients(Long dishId, List<DishCreateCommand.Ingredient> ingredients) {
        ingredientMapper.delete(new QueryWrapper<DishIngredient>().eq("dish_id", dishId));
        for (DishCreateCommand.Ingredient item : Optional.ofNullable(ingredients).orElse(List.of())) {
            DishIngredient ingredient = new DishIngredient();
            ingredient.setDishId(dishId);
            ingredient.setName(item.name());
            ingredient.setAmount(item.amount());
            ingredient.setUnit(item.unit());
            ingredient.setCategory(item.category());
            ingredient.setRequired(Boolean.FALSE.equals(item.required()) ? 0 : 1);
            ingredient.setNote(item.note());
            ingredientMapper.insert(ingredient);
        }
    }

    private List<DishResponse> enrich(List<DishView> dishes) {
        if (dishes.isEmpty()) {
            return List.of();
        }
        Set<Long> dishIds = dishes.stream().map(Dish::getId).collect(Collectors.toSet());
        Map<Long, List<DishIngredient>> ingredients = ingredientMapper.selectByDishIds(dishIds)
                .stream().collect(Collectors.groupingBy(DishIngredient::getDishId));
        Map<Long, List<DishTag>> tagsByDish = tagMapper.selectByDishIds(dishIds).stream()
                .collect(Collectors.groupingBy(DishTagView::getDishId,
                        Collectors.mapping(tag -> (DishTag) tag, Collectors.toList())));
        return dishes.stream().map(dish -> new DishResponse(
                dish.getId(), dish.getName(), dish.getAliases(), dish.getDescription(), dish.getImageUrl(),
                dish.getCategoryId(), dish.getCategoryName(), tagsByDish.getOrDefault(dish.getId(), List.of()),
                split(dish.getMealTypes()), dish.getTaste(), dish.getDifficulty(), dish.getEstimatedMinutes(),
                dish.getDefaultServings(), dish.getInstructions(), dish.getSourceType(), dish.getSourceUrl(),
                dish.getStatus(), ingredients.getOrDefault(dish.getId(), List.of())
        )).toList();
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }
}
