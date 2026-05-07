package com.familyfood.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.ai.dao.AiExtractedDishMapper;
import com.familyfood.ai.dao.AiRecommendationMapper;
import com.familyfood.ai.dao.AiSourceContentMapper;
import com.familyfood.ai.dao.AiTaskMapper;
import com.familyfood.ai.dto.AiTaskDetail;
import com.familyfood.ai.dto.ConvertRequest;
import com.familyfood.ai.dto.ConvertResponse;
import com.familyfood.ai.dto.MenuPlanRequest;
import com.familyfood.ai.dto.ParseLinkRequest;
import com.familyfood.ai.dto.ParseLinkResponse;
import com.familyfood.ai.dto.RecommendationDto;
import com.familyfood.ai.dto.RecommendationRequest;
import com.familyfood.ai.dto.RecommendationResponse;
import com.familyfood.ai.entity.AiExtractedDish;
import com.familyfood.ai.entity.AiRecommendation;
import com.familyfood.ai.entity.AiSourceContent;
import com.familyfood.ai.entity.AiTask;
import com.familyfood.ai.provider.AiProvider;
import com.familyfood.ai.service.AiTaskProcessor;
import com.familyfood.ai.service.AiTaskService;
import com.familyfood.ai.support.SourceTypeDetector;
import com.familyfood.ai.support.UrlSafety;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.Enums.SourceType;
import com.familyfood.common.StatusValues;
import com.familyfood.dish.api.DishCandidate;
import com.familyfood.dish.api.DishCatalogApi;
import com.familyfood.dish.api.DishCreateCommand;
import com.familyfood.dish.entity.Dish;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional(readOnly = true)
public class AiTaskServiceImpl implements AiTaskService {
    private final AiTaskMapper taskMapper;
    private final AiSourceContentMapper contentMapper;
    private final AiExtractedDishMapper extractedDishMapper;
    private final AiRecommendationMapper recommendationMapper;
    private final DishCatalogApi dishCatalogApi;
    private final SourceTypeDetector detector;
    private final UrlSafety urlSafety;
    private final AiTaskProcessor taskProcessor;
    private final AiProvider provider;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiTaskServiceImpl(AiTaskMapper taskMapper, AiSourceContentMapper contentMapper,
                         AiExtractedDishMapper extractedDishMapper, AiRecommendationMapper recommendationMapper,
                         DishCatalogApi dishCatalogApi, SourceTypeDetector detector, UrlSafety urlSafety,
                         AiTaskProcessor taskProcessor, AiProvider provider, ObjectMapper objectMapper) {
        this.taskMapper = taskMapper;
        this.contentMapper = contentMapper;
        this.extractedDishMapper = extractedDishMapper;
        this.recommendationMapper = recommendationMapper;
        this.dishCatalogApi = dishCatalogApi;
        this.detector = detector;
        this.urlSafety = urlSafety;
        this.taskProcessor = taskProcessor;
        this.provider = provider;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ParseLinkResponse parseLink(ActorContext actor, ParseLinkRequest request) {
        SourceType sourceType = detector.detect(request.url(), request.fallbackText(), request.imageUrl());
        AiTask task = createTask(actor, "PARSE_LINK", sourceType.name(), request.fallbackText(),
                request.url(), request.imageUrl(), "PENDING");
        try {
            urlSafety.validatePublicHttpUrl(request.url());
            processParseTaskAfterCommit(task.getId(), request);
        } catch (AppException ex) {
            taskProcessor.markReviewRequired(task.getId(), "SOURCE_UNAVAILABLE", ex.getMessage(),
                    request.url(), request.fallbackText());
            task.setStatus("REVIEW_REQUIRED");
        }
        return new ParseLinkResponse(task.getId(), task.getStatus(), sourceType.name(), "/api/ai/tasks/" + task.getId());
    }

    @Override
    @Transactional
    public RecommendationResponse recommend(ActorContext actor, RecommendationRequest request) {
        int maxResults = request.maxResults() == null ? 6 : request.maxResults();
        AiTask task = createTask(actor, "RECOMMEND", "TEXT", request.prompt(), null, null, "RECOMMENDING");
        List<AiProvider.CandidateDish> candidates = dishCatalogApi.activeCandidates(actor, 50).stream()
                .map(this::toProviderCandidate)
                .toList();
        AiProvider.AiRecommendationResult result = provider.recommend(new AiProvider.AiRecommendationRequest(
                request.prompt(), request.mealType(), maxResults, candidates));
        List<RecommendationDto> responses = new java.util.ArrayList<>();
        for (AiProvider.RecommendationDto dto : result.recommendations()) {
            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setAiTaskId(task.getId());
            recommendation.setFamilyId(actor.familyId());
            recommendation.setUserId(actor.userId());
            recommendation.setPrompt(request.prompt());
            recommendation.setDishId(dto.dishId());
            recommendation.setTitle(dto.title());
            recommendation.setReason(dto.reason());
            recommendation.setScore(BigDecimal.valueOf(dto.score()));
            recommendation.setCreatedAt(LocalDateTime.now());
            recommendationMapper.insert(recommendation);
            responses.add(new RecommendationDto(dto.type(), dto.dishId(), null, dto.title(), dto.reason(), dto.score()));
        }
        if (responses.size() < 3) {
            AiProvider.AiStructuredResult extracted = provider.extractDishes(new AiProvider.AiExtractionRequest(
                    "TEXT", null, null, request.prompt(), request.prompt(),
                    dishCatalogApi.existingDishNames(actor.familyId(), 100)));
            for (AiProvider.ExtractedDishDto dto : extracted.dishes()) {
                AiExtractedDish draft = toExtracted(task, dto);
                extractedDishMapper.insert(draft);
                responses.add(new RecommendationDto("AI_DRAFT", null, draft.getId(),
                        draft.getName(), draft.getRecommendationReason(), 0.82));
            }
        }
        task.setStatus("SUCCESS");
        task.setResultSummary("Generated " + responses.size() + " recommendations");
        task.setFinishedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return new RecommendationResponse(task.getId(), responses);
    }

    @Override
    @Transactional(readOnly = true)
    public AiTaskDetail detail(ActorContext actor, Long taskId) {
        AiTask task = taskMapper.selectById(taskId);
        ensureTaskAccess(actor, task);
        AiSourceContent content = contentMapper.selectOne(new QueryWrapper<AiSourceContent>()
                .eq("ai_task_id", taskId).last("limit 1"));
        List<AiExtractedDish> extracted = extractedDishMapper.selectList(
                new QueryWrapper<AiExtractedDish>().eq("ai_task_id", taskId));
        List<AiRecommendation> recommendations = recommendationMapper.selectList(
                new QueryWrapper<AiRecommendation>().eq("ai_task_id", taskId));
        return new AiTaskDetail(task, content, extracted, recommendations);
    }

    @Transactional(readOnly = true)
    public List<AiTask> tasks(ActorContext actor, String status, String sourceType) {
        String normalizedStatus = StatusValues.optional(status, StatusValues.AI_TASK_STATUSES, "status");
        String normalizedSourceType = StatusValues.optional(sourceType, StatusValues.SOURCE_TYPES, "sourceType");
        QueryWrapper<AiTask> wrapper = new QueryWrapper<AiTask>()
                .eq("family_id", actor.familyId())
                .orderByDesc("created_at");
        if (!actor.admin()) {
            wrapper.eq("user_id", actor.userId());
        }
        if (normalizedStatus != null) {
            wrapper.eq("status", normalizedStatus);
        }
        if (normalizedSourceType != null) {
            wrapper.eq("source_type", normalizedSourceType);
        }
        return taskMapper.selectList(wrapper);
    }

    @Transactional(readOnly = true)
    public List<AiExtractedDish> drafts(ActorContext actor, String reviewStatus) {
        actor.requireAdmin();
        QueryWrapper<AiExtractedDish> wrapper = new QueryWrapper<AiExtractedDish>()
                .eq("family_id", actor.familyId())
                .orderByDesc("created_at");
        if (reviewStatus != null && !reviewStatus.isBlank()) {
            wrapper.eq("review_status", reviewStatus);
        }
        return extractedDishMapper.selectList(wrapper);
    }

    @Transactional
    public AiTask retry(ActorContext actor, Long taskId) {
        AiTask task = taskMapper.selectById(taskId);
        ensureTaskAccess(actor, task);
        if (!"PARSE_LINK".equals(task.getTaskType())) {
            throw AppException.badRequest("只有链接解析任务可以重试，请重新选择任务");
        }
        task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
        task.setStatus("PENDING");
        task.setErrorCode(null);
        task.setErrorMessage(null);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        ParseLinkRequest retryRequest = new ParseLinkRequest(task.getSourceUrl(), task.getInputText(), task.getImageUrl());
        try {
            urlSafety.validatePublicHttpUrl(retryRequest.url());
            processParseTaskAfterCommit(task.getId(), retryRequest);
        } catch (AppException ex) {
            taskProcessor.markReviewRequired(task.getId(), "SOURCE_UNAVAILABLE", ex.getMessage(),
                    retryRequest.url(), retryRequest.fallbackText());
        }
        return taskMapper.selectById(taskId);
    }

    @Transactional
    public ConvertResponse convert(ActorContext actor, Long id, ConvertRequest request) {
        actor.requireAdmin();
        AiExtractedDish draft = extractedDishMapper.selectById(id);
        if (draft == null || !Objects.equals(draft.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到这份 AI 菜品草稿，请刷新后再试");
        }
        String status = request.override() == null ? "ACTIVE" :
                StatusValues.orDefault(request.override().status(), "ACTIVE", StatusValues.DISH_STATUSES, "override.status");
        Long categoryId = request.override() == null
                ? dishCatalogApi.findCategoryId(actor.familyId(), draft.getCategoryName(), "Home dishes")
                : request.override().categoryId();
        Dish dish = dishCatalogApi.createDish(actor, new DishCreateCommand(
                categoryId,
                request.override() != null && request.override().name() != null ? request.override().name() : draft.getName(),
                draft.getAliases(),
                draft.getRecommendationReason(),
                null,
                draft.getTaste(),
                parseStringArray(draft.getMealTypesJson()),
                draft.getDifficulty(),
                draft.getEstimatedMinutes(),
                BigDecimal.ONE,
                draft.getInstructions(),
                "AI",
                null,
                status,
                parseIngredients(draft.getIngredientsJson()).stream()
                        .map(i -> new DishCreateCommand.Ingredient(
                                i.name(), BigDecimal.valueOf(i.amount()), i.unit(), i.category(), i.required(), null))
                        .toList()
        ));
        draft.setReviewStatus("ACCEPTED");
        draft.setConvertedDishId(dish.getId());
        draft.setUpdatedAt(LocalDateTime.now());
        extractedDishMapper.updateById(draft);
        return new ConvertResponse(dish.getId(), draft.getReviewStatus());
    }

    public AiProvider.AiMenuPlanResult menuPlan(ActorContext actor, MenuPlanRequest request) {
        List<AiProvider.CandidateDish> candidates = dishCatalogApi.activeCandidates(actor, 20).stream()
                .map(this::toProviderCandidate)
                .toList();
        return provider.planMenu(new AiProvider.AiMenuPlanRequest(request.prompt(), request.mealType(),
                request.peopleCount() == null ? 1 : request.peopleCount(), request.avoidances(), candidates));
    }

    private AiTask createTask(ActorContext actor, String type, String sourceType, String input,
                              String url, String imageUrl, String status) {
        LocalDateTime now = LocalDateTime.now();
        AiTask task = new AiTask();
        task.setFamilyId(actor.familyId());
        task.setUserId(actor.userId());
        task.setTaskType(type);
        task.setSourceType(sourceType);
        task.setInputText(input);
        task.setSourceUrl(url);
        task.setImageUrl(imageUrl);
        task.setStatus(status);
        task.setRetryCount(0);
        task.setModelName(provider.modelName());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        taskMapper.insert(task);
        return task;
    }

    private void processParseTaskAfterCommit(Long taskId, ParseLinkRequest request) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            taskProcessor.processParseTask(taskId, request);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskProcessor.processParseTask(taskId, request);
            }
        });
    }

    private AiExtractedDish toExtracted(AiTask task, AiProvider.ExtractedDishDto dto) {
        AiExtractedDish dish = new AiExtractedDish();
        dish.setAiTaskId(task.getId());
        dish.setFamilyId(task.getFamilyId());
        dish.setName(dto.name());
        dish.setCategoryName("Home dishes");
        dish.setTagsJson(toJson(dto.tags()));
        dish.setTaste(dto.taste());
        dish.setMealTypesJson(toJson(dto.mealTypes()));
        dish.setDifficulty(dto.difficulty());
        dish.setEstimatedMinutes(dto.estimatedMinutes());
        dish.setIngredientsJson(toJson(dto.ingredients()));
        dish.setInstructions(dto.instructions());
        dish.setRecommendationReason(dto.reason());
        dish.setConfidence(BigDecimal.valueOf(dto.confidence()));
        dish.setReviewStatus("PENDING");
        dish.setCreatedAt(LocalDateTime.now());
        dish.setUpdatedAt(LocalDateTime.now());
        return dish;
    }

    private AiProvider.CandidateDish toProviderCandidate(DishCandidate dish) {
        return new AiProvider.CandidateDish(dish.id(), dish.name(), dish.tags(), dish.taste(), dish.mealTypes());
    }

    private void ensureTaskAccess(ActorContext actor, AiTask task) {
        if (task == null || !Objects.equals(task.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到这个 AI 任务，请刷新后再试");
        }
        if (!actor.admin() && !Objects.equals(task.getUserId(), actor.userId())) {
            throw AppException.forbidden("你没有权限查看这个 AI 任务");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private List<String> parseStringArray(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readerForListOf(String.class).readValue(json);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<AiProvider.IngredientDto> parseIngredients(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readerForListOf(AiProvider.IngredientDto.class).readValue(json);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }
}
