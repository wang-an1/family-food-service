package com.familyfood.ai.service.impl;

import com.familyfood.ai.dao.AiExtractedDishMapper;
import com.familyfood.ai.dao.AiSourceContentMapper;
import com.familyfood.ai.dao.AiTaskMapper;
import com.familyfood.ai.dto.ParseLinkRequest;
import com.familyfood.ai.entity.AiExtractedDish;
import com.familyfood.ai.entity.AiSourceContent;
import com.familyfood.ai.entity.AiTask;
import com.familyfood.ai.provider.AiProvider;
import com.familyfood.ai.service.AiTaskProcessor;
import com.familyfood.common.AppException;
import com.familyfood.dish.api.DishCatalogApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiTaskProcessorImpl implements AiTaskProcessor {
    private static final Logger log = LoggerFactory.getLogger(AiTaskProcessorImpl.class);

    private final AiTaskMapper taskMapper;
    private final AiSourceContentMapper contentMapper;
    private final AiExtractedDishMapper extractedDishMapper;
    private final DishCatalogApi dishCatalogApi;
    private final AiProvider provider;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiTaskProcessorImpl(AiTaskMapper taskMapper, AiSourceContentMapper contentMapper,
                           AiExtractedDishMapper extractedDishMapper, DishCatalogApi dishCatalogApi,
                           AiProvider provider, ObjectMapper objectMapper) {
        this.taskMapper = taskMapper;
        this.contentMapper = contentMapper;
        this.extractedDishMapper = extractedDishMapper;
        this.dishCatalogApi = dishCatalogApi;
        this.provider = provider;
        this.objectMapper = objectMapper;
    }

    @Async
    public void processParseTask(Long taskId, ParseLinkRequest request) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        log.info("ai_parse_task_started taskId={} sourceType={} url={}", taskId, task.getSourceType(), request.url());
        try {
            updateTask(task, "FETCHING_CONTENT", null, null);
            PageContent page = fetchPage(request.url(), request.fallbackText());
            AiSourceContent content = new AiSourceContent();
            content.setAiTaskId(taskId);
            content.setResolvedUrl(request.url());
            content.setTitle(page.title());
            content.setDescription(page.description());
            content.setContentText(page.contentText());
            contentMapper.insert(content);

            updateTask(task, "AI_EXTRACTING", null, null);
            AiProvider.AiStructuredResult result = provider.extractDishes(new AiProvider.AiExtractionRequest(
                    task.getSourceType(), page.title(), page.description(), page.contentText(),
                    request.fallbackText(), existingDishNames(task.getFamilyId())));
            for (AiProvider.ExtractedDishDto dto : result.dishes()) {
                extractedDishMapper.insert(toExtracted(task, dto));
            }
            task.setResultSummary(result.summary());
            task.setStatus("REVIEW_REQUIRED");
            task.setFinishedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);
            log.info("ai_parse_task_review_required taskId={} extractedCount={}", taskId, result.dishes().size());
        } catch (AppException ex) {
            if ("AI_UNAVAILABLE".equals(ex.code()) || "AI_PROVIDER_ERROR".equals(ex.code())) {
                log.warn("ai_parse_task_provider_failed taskId={} code={} url={}", taskId, ex.code(), request.url(), ex);
                markFailed(taskId, ex.code(), ex.getMessage());
                return;
            }
            log.warn("ai_parse_task_recoverable_failed taskId={} code={} url={}", taskId, ex.code(), request.url(), ex);
            markReviewRequired(taskId, "SOURCE_UNAVAILABLE", "无法自动解析，请补充标题、正文或截图",
                    request.url(), request.fallbackText());
        } catch (Exception ex) {
            log.warn("ai_parse_task_recoverable_failed taskId={} url={}", taskId, request.url(), ex);
            markReviewRequired(taskId, "SOURCE_UNAVAILABLE", "无法自动解析，请补充标题、正文或截图",
                    request.url(), request.fallbackText());
        }
    }

    @Transactional
    public void markReviewRequired(Long taskId, String code, String message, String url, String fallbackText) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        AiSourceContent content = new AiSourceContent();
        content.setAiTaskId(taskId);
        content.setResolvedUrl(url);
        content.setTitle(fallbackText == null || fallbackText.isBlank() ? "需要人工补充内容" : fallbackText);
        content.setDescription(message);
        contentMapper.insert(content);
        task.setStatus("REVIEW_REQUIRED");
        task.setErrorCode(code);
        task.setErrorMessage(message);
        task.setResultSummary("无法自动解析，请补充标题、正文或截图");
        task.setFinishedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Transactional
    public void markFailed(Long taskId, String code, String message) {
        AiTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        task.setStatus("FAILED");
        task.setErrorCode(code);
        task.setErrorMessage(message);
        task.setResultSummary(message);
        task.setFinishedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void updateTask(AiTask task, String status, String errorCode, String errorMessage) {
        task.setStatus(status);
        task.setErrorCode(errorCode);
        task.setErrorMessage(errorMessage);
        task.setStartedAt(task.getStartedAt() == null ? LocalDateTime.now() : task.getStartedAt());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private PageContent fetchPage(String url, String fallbackText) {
        if (url == null || url.isBlank()) {
            return new PageContent(fallbackText, null, fallbackText);
        }
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("family-food/0.1 (+https://localhost)")
                    .timeout(3000)
                    .maxBodySize(512_000)
                    .followRedirects(true)
                    .get();
            Element descriptionMeta = document.selectFirst("meta[name=description]");
            String title = document.title();
            String description = descriptionMeta == null ? null : descriptionMeta.attr("content");
            String text = document.body() == null ? fallbackText : document.body().text();
            return new PageContent(blankToFallback(title, fallbackText), description,
                    text == null || text.isBlank() ? fallbackText : text);
        } catch (Exception ex) {
            log.info("ai_fetch_page_failed url={}", url, ex);
            return new PageContent(fallbackText == null ? "公开页面无法访问" : fallbackText, "自动解析失败", fallbackText);
        }
    }

    private String blankToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private AiExtractedDish toExtracted(AiTask task, AiProvider.ExtractedDishDto dto) {
        AiExtractedDish dish = new AiExtractedDish();
        dish.setAiTaskId(task.getId());
        dish.setFamilyId(task.getFamilyId());
        dish.setName(dto.name());
        dish.setCategoryName("家常菜");
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

    private List<String> existingDishNames(Long familyId) {
        return dishCatalogApi.existingDishNames(familyId, 100);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private record PageContent(String title, String description, String contentText) {
    }
}
