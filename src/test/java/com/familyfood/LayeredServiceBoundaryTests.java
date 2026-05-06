package com.familyfood;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.ai.dao.AiExtractedDishMapper;
import com.familyfood.ai.dao.AiTaskMapper;
import com.familyfood.ai.dto.ConvertRequest;
import com.familyfood.ai.entity.AiExtractedDish;
import com.familyfood.ai.entity.AiTask;
import com.familyfood.ai.service.AiTaskService;
import com.familyfood.auth.security.UserPrincipal;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.dish.api.DishCreateCommand;
import com.familyfood.dish.dao.DishIngredientMapper;
import com.familyfood.dish.dao.DishMapper;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishIngredient;
import com.familyfood.dish.service.DishCatalogService;
import com.familyfood.intent.dto.IntentResponse;
import com.familyfood.intent.dto.IntentSubmitRequest;
import com.familyfood.intent.service.IntentApplicationService;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.dto.OrderItemRequest;
import com.familyfood.order.dto.OrderRequest;
import com.familyfood.order.entity.MealSession;
import com.familyfood.order.service.MealSessionService;
import com.familyfood.order.service.OrderDomainService;
import com.familyfood.shopping.dao.ShoppingListItemMapper;
import com.familyfood.shopping.dto.ShoppingResponse;
import com.familyfood.shopping.entity.ShoppingListItem;
import com.familyfood.shopping.service.ShoppingListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LayeredServiceBoundaryTests {
    private static final ActorContext ADMIN = new ActorContext(1L, 1L, "ADMIN", true);
    private static final ActorContext MEMBER = new ActorContext(1L, 2L, "MEMBER", false);

    @Autowired
    DishCatalogService dishCatalogService;
    @Autowired
    MealSessionService mealSessionService;
    @Autowired
    OrderDomainService orderDomainService;
    @Autowired
    ShoppingListService shoppingListService;
    @Autowired
    AiTaskService aiTaskService;
    @Autowired
    IntentApplicationService intentApplicationService;
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishIngredientMapper ingredientMapper;
    @Autowired
    ShoppingListItemMapper shoppingItemMapper;
    @Autowired
    AiTaskMapper taskMapper;
    @Autowired
    AiExtractedDishMapper extractedDishMapper;
    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void orderSubmitRejectsUnavailableDishThroughCatalogApi() {
        MealSession session = createSession("boundary unavailable");
        Dish inactive = createDish("boundary inactive " + System.nanoTime(), "INACTIVE", "boundary ingredient");

        AppException ex = assertThrows(AppException.class, () -> orderDomainService.submit(MEMBER,
                orderRequest(session.getId(), inactive.getId(), BigDecimal.ONE)));

        assertEquals("VALIDATION_ERROR", ex.code());
    }

    @Test
    void shoppingGenerateUsesMealSessionApiAndPreservesCarriedState() {
        String ingredientName = "boundary ingredient " + System.nanoTime();
        MealSession session = createSession("boundary shopping");
        Dish dish = createDish("boundary dish " + System.nanoTime(), "ACTIVE", ingredientName);
        orderDomainService.submit(MEMBER, orderRequest(session.getId(), dish.getId(), BigDecimal.valueOf(2)));

        ShoppingResponse first = shoppingListService.generate(ADMIN, session.getId());
        ShoppingListItem original = findItem(first, ingredientName);
        shoppingListService.check(ADMIN, original.getId(), true);
        original = shoppingItemMapper.selectById(original.getId());
        original.setNote("keep this note");
        shoppingItemMapper.updateById(original);

        ShoppingResponse second = shoppingListService.generate(ADMIN, session.getId());
        ShoppingListItem carried = findItem(second, ingredientName);

        assertEquals(1, carried.getChecked());
        assertEquals("keep this note", carried.getNote());
    }

    @Test
    void aiDraftConvertCreatesDishThroughCatalogApi() throws Exception {
        AiTask task = createAiTask();
        AiExtractedDish draft = new AiExtractedDish();
        draft.setAiTaskId(task.getId());
        draft.setFamilyId(ADMIN.familyId());
        draft.setName("boundary ai dish " + System.nanoTime());
        draft.setTaste("light");
        draft.setMealTypesJson(objectMapper.writeValueAsString(List.of("DINNER")));
        draft.setDifficulty("EASY");
        draft.setEstimatedMinutes(15);
        draft.setIngredientsJson(objectMapper.writeValueAsString(List.of(Map.of(
                "name", "boundary ai ingredient",
                "amount", 1,
                "unit", "item",
                "category", "test",
                "required", true
        ))));
        draft.setInstructions("cook");
        draft.setRecommendationReason("test convert");
        draft.setConfidence(BigDecimal.valueOf(0.9));
        draft.setReviewStatus("PENDING");
        draft.setCreatedAt(LocalDateTime.now());
        draft.setUpdatedAt(LocalDateTime.now());
        extractedDishMapper.insert(draft);

        Long dishId = aiTaskService.convert(ADMIN, draft.getId(), new ConvertRequest("CREATE", null, null)).dishId();

        Dish dish = dishMapper.selectById(dishId);
        assertNotNull(dish);
        assertEquals("AI", dish.getSourceType());
        assertEquals("ACCEPTED", extractedDishMapper.selectById(draft.getId()).getReviewStatus());
        List<DishIngredient> ingredients = ingredientMapper.selectList(new QueryWrapper<DishIngredient>().eq("dish_id", dishId));
        assertEquals(1, ingredients.size());
        assertEquals("boundary ai ingredient", ingredients.get(0).getName());
    }

    @Test
    void intentSubmitDispatchesTextLinkAndImageToAiTaskApi() {
        authenticateMember();

        IntentResponse text = intentApplicationService.submit(
                new IntentSubmitRequest(null, "boundary dinner idea", null, null, null));
        IntentResponse link = intentApplicationService.submit(
                new IntentSubmitRequest(null, "boundary link", "http://127.0.0.1/private", null, null));
        IntentResponse image = intentApplicationService.submit(
                new IntentSubmitRequest(null, null, null, "/uploads/intent/boundary.png", null));

        assertEquals("RECOMMEND", taskMapper.selectById(text.aiTaskId()).getTaskType());
        assertEquals("PARSE_LINK", taskMapper.selectById(link.aiTaskId()).getTaskType());
        assertEquals("PARSE_LINK", taskMapper.selectById(image.aiTaskId()).getTaskType());
    }

    private MealSession createSession(String title) {
        return mealSessionService.create(ADMIN, new MealSessionRequest(
                title + " " + System.nanoTime(), "DINNER", LocalDate.now(), null, "OPEN", false));
    }

    private Dish createDish(String name, String status, String ingredientName) {
        return dishCatalogService.createDish(ADMIN, new DishCreateCommand(
                1L,
                name,
                null,
                "boundary test dish",
                null,
                "light",
                List.of("DINNER"),
                "EASY",
                10,
                BigDecimal.ONE,
                "cook",
                "MANUAL",
                null,
                status,
                List.of(new DishCreateCommand.Ingredient(
                        ingredientName, BigDecimal.ONE, "item", "test", true, null))
        ));
    }

    private OrderRequest orderRequest(Long sessionId, Long dishId, BigDecimal quantity) {
        return new OrderRequest(sessionId, null, null, null,
                List.of(new OrderItemRequest(dishId, quantity, "item", null)));
    }

    private ShoppingListItem findItem(ShoppingResponse response, String ingredientName) {
        return response.items().stream()
                .filter(item -> ingredientName.equals(item.getName()) && "DISH".equals(item.getSource()))
                .findFirst()
                .orElseThrow();
    }

    private AiTask createAiTask() {
        AiTask task = new AiTask();
        task.setFamilyId(ADMIN.familyId());
        task.setUserId(ADMIN.userId());
        task.setTaskType("PARSE_LINK");
        task.setSourceType("TEXT");
        task.setStatus("REVIEW_REQUIRED");
        task.setRetryCount(0);
        task.setModelName("mock");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return task;
    }

    private void authenticateMember() {
        UserPrincipal principal = new UserPrincipal(2L, 1L, "member", "member", "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities()));
    }
}
