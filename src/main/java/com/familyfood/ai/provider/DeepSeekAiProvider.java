package com.familyfood.ai.provider;

import com.familyfood.ai.dto.RecommendationDto;
import com.familyfood.common.AppException;
import com.familyfood.config.AppProperties;
import com.familyfood.system.api.SystemConfigApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DeepSeekAiProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekAiProvider.class);
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-v4";

    private final AppProperties properties;
    private final SystemConfigApi configService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public DeepSeekAiProvider(AppProperties properties, SystemConfigApi configService, ObjectMapper objectMapper) {
        this.properties = properties;
        this.configService = configService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(properties.ai().timeoutSeconds()))
                .build();
    }

    @Override
    public String modelName() {
        return chatModel();
    }

    public boolean isConfigured() {
        return !apiKey().isBlank();
    }

    @Override
    public AiStructuredResult extractDishes(AiExtractionRequest request) {
        String content = chat(String.join("\n",
                "你是家庭点餐系统的菜品抽取助手。只输出 JSON，不要 Markdown。",
                "JSON 结构：",
                "{",
                "  \"summary\": \"一句话概括\",",
                "  \"dishes\": [",
                "    {",
                "      \"name\": \"菜名\",",
                "      \"tags\": [\"快手菜\"],",
                "      \"taste\": \"清淡\",",
                "      \"mealTypes\": [\"LUNCH\", \"DINNER\"],",
                "      \"difficulty\": \"EASY\",",
                "      \"estimatedMinutes\": 20,",
                "      \"ingredients\": [{\"name\": \"鸡蛋\", \"amount\": 3, \"unit\": \"个\", \"category\": \"蛋类\", \"required\": true}],",
                "      \"instructions\": \"做法\",",
                "      \"reason\": \"推荐理由\",",
                "      \"confidence\": 0.86",
                "    }",
                "  ]",
                "}",
                "difficulty 只能是 EASY、MEDIUM、HARD；mealTypes 只能从 BREAKFAST、LUNCH、DINNER、SNACK 中选择。"
        ), extractionPrompt(request), true);
        JsonNode root = readJson(content);
        List<ExtractedDishDto> dishes = new ArrayList<>();
        for (JsonNode item : root.path("dishes")) {
            List<IngredientDto> ingredients = new ArrayList<>();
            for (JsonNode ingredient : item.path("ingredients")) {
                ingredients.add(new IngredientDto(
                        text(ingredient, "name", "食材"),
                        number(ingredient, "amount", 1),
                        text(ingredient, "unit", "份"),
                        text(ingredient, "category", "其他"),
                        !ingredient.has("required") || ingredient.path("required").asBoolean()
                ));
            }
            dishes.add(new ExtractedDishDto(
                    text(item, "name", "AI 候选菜"),
                    stringList(item.path("tags")),
                    text(item, "taste", "家常"),
                    normalizeMealTypes(stringList(item.path("mealTypes"))),
                    normalizeDifficulty(text(item, "difficulty", "EASY")),
                    item.path("estimatedMinutes").asInt(20),
                    ingredients,
                    text(item, "instructions", "按常规家常做法处理食材并调味。"),
                    text(item, "reason", "DeepSeek 根据输入内容生成。"),
                    clamp(item.path("confidence").asDouble(0.82))
            ));
        }
        return new AiStructuredResult(text(root, "summary", "DeepSeek 已生成候选菜。"), dishes);
    }

    @Override
    public AiRecommendationResult recommend(AiRecommendationRequest request) {
        if (request.candidateDishes().isEmpty()) {
            return new AiRecommendationResult(List.of(), "菜品库为空，请先维护常用菜。");
        }
        String content = chat(String.join("\n",
                "你是家庭点餐系统的菜品推荐助手。只输出 JSON，不要 Markdown。",
                "JSON 结构：",
                "{",
                "  \"recommendations\": [",
                "    {\"type\": \"EXISTING_DISH\", \"dishId\": 1, \"title\": \"菜名\", \"reason\": \"推荐理由\", \"score\": 0.92}",
                "  ],",
                "  \"fallbackMessage\": \"\"",
                "}",
                "只能推荐候选菜列表中存在的 dishId，score 范围 0 到 1。"
        ), recommendationPrompt(request), true);
        JsonNode root = readJson(content);
        List<RecommendationDto> recommendations = new ArrayList<>();
        for (JsonNode item : root.path("recommendations")) {
            Long dishId = item.path("dishId").canConvertToLong() ? item.path("dishId").asLong() : null;
            if (dishId == null) {
                continue;
            }
            recommendations.add(new RecommendationDto(
                    text(item, "type", "EXISTING_DISH"),
                    dishId,
                    text(item, "title", "推荐菜"),
                    text(item, "reason", "匹配当前家庭口味和餐次。"),
                    clamp(item.path("score").asDouble(0.82))
            ));
        }
        return new AiRecommendationResult(recommendations, text(root, "fallbackMessage", null));
    }

    @Override
    public AiMenuPlanResult planMenu(AiMenuPlanRequest request) {
        String content = chat(String.join("\n",
                "你是家庭点餐系统的菜单搭配助手。只输出 JSON，不要 Markdown。",
                "JSON 结构：",
                "{",
                "  \"planTitle\": \"菜单标题\",",
                "  \"items\": [{\"dishId\": 1, \"name\": \"菜名\", \"role\": \"主菜\", \"reason\": \"搭配理由\"}],",
                "  \"shoppingSummary\": \"采购提醒\"",
                "}",
                "优先使用候选菜里的 dishId；不要编造不存在的 dishId。"
        ), menuPrompt(request), true);
        JsonNode root = readJson(content);
        List<MenuPlanItem> items = new ArrayList<>();
        for (JsonNode item : root.path("items")) {
            Long dishId = item.path("dishId").canConvertToLong() ? item.path("dishId").asLong() : null;
            items.add(new MenuPlanItem(
                    dishId,
                    text(item, "name", "搭配菜"),
                    text(item, "role", "菜品"),
                    text(item, "reason", "与当前餐次搭配。")
            ));
        }
        return new AiMenuPlanResult(
                text(root, "planTitle", "DeepSeek 菜单搭配"),
                items,
                text(root, "shoppingSummary", "按菜单检查主要食材是否充足。")
        );
    }

    @Override
    public String summarize(AiSummarizeRequest request) {
        return chat("用一句中文概括以下公开页面内容，不超过 60 字。", safe(request.title()) + "\n" + safe(request.contentText()), false);
    }

    private String chat(String systemPrompt, String userPrompt, boolean jsonMode) {
        String key = apiKey();
        if (key.isBlank()) {
            throw AppException.badRequest("请先在系统配置中设置 DeepSeek 密钥");
        }
        String endpoint = chatEndpoint();
        String model = chatModel();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", 0.35);
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        long started = System.currentTimeMillis();
        try {
            JsonNode response = restClient.post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + key)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            String content = response == null ? null : response.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                log.warn("ai_provider_empty_response provider=deepseek model={} endpoint={} durationMs={}",
                        model, endpoint, System.currentTimeMillis() - started);
                throw AppException.serviceUnavailable("AI_PROVIDER_ERROR", "AI 服务返回为空，请稍后重试");
            }
            log.info("ai_provider_call_success provider=deepseek model={} endpoint={} jsonMode={} durationMs={}",
                    model, endpoint, jsonMode, System.currentTimeMillis() - started);
            return content.trim();
        } catch (RestClientException ex) {
            log.warn("ai_provider_call_failed provider=deepseek model={} endpoint={} durationMs={}",
                    model, endpoint, System.currentTimeMillis() - started, ex);
            throw AppException.serviceUnavailable("AI_UNAVAILABLE", "AI 服务暂时不可用，请稍后重试");
        }
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private String extractionPrompt(AiExtractionRequest request) {
        return String.join("\n",
                "平台：" + safe(request.sourceType()),
                "标题：" + safe(request.title()),
                "描述：" + safe(request.description()),
                "正文：" + trim(safe(request.contentText()), 3000),
                "用户补充：" + safe(request.fallbackText()),
                "已有菜品名：" + request.existingDishNames()
        );
    }

    private String recommendationPrompt(AiRecommendationRequest request) {
        return String.join("\n",
                "用户需求：" + safe(request.prompt()),
                "餐次：" + safe(request.mealType()),
                "最多推荐：" + request.maxResults(),
                "候选菜 JSON：" + toJson(request.candidateDishes())
        );
    }

    private String menuPrompt(AiMenuPlanRequest request) {
        return String.join("\n",
                "需求：" + safe(request.prompt()),
                "餐次：" + safe(request.mealType()),
                "人数：" + request.peopleCount(),
                "忌口：" + safe(request.avoidances()),
                "候选菜 JSON：" + toJson(request.candidates())
        );
    }

    private String chatEndpoint() {
        String baseUrl = configService.value("ai.base_url", propertyBaseUrl());
        String normalized = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
        return normalized.replaceAll("/+$", "") + "/chat/completions";
    }

    private String apiKey() {
        return configService.value("ai.api_key", properties.ai().apiKey() == null ? "" : properties.ai().apiKey()).trim();
    }

    private String chatModel() {
        return configService.value("ai.chat_model", propertyModel());
    }

    private String propertyBaseUrl() {
        return properties.ai() == null || properties.ai().baseUrl() == null ? DEFAULT_BASE_URL : properties.ai().baseUrl();
    }

    private String propertyModel() {
        return properties.ai() == null || properties.ai().chatModel() == null ? DEFAULT_MODEL : properties.ai().chatModel();
    }

    private JsonNode readJson(String content) {
        String json = content.trim();
        if (!json.startsWith("{")) {
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw AppException.serviceUnavailable("AI_PROVIDER_ERROR", "AI 服务返回格式无法解析，请稍后重试");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        if (node == null || !node.has(field) || node.path(field).isNull()) {
            return fallback;
        }
        String value = node.path(field).asText();
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<String> stringList(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                String value = item.asText();
                if (value != null && !value.isBlank()) {
                    values.add(value);
                }
            }
            return values;
        }
        String value = node.asText("");
        if (value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("[,，、]"));
    }

    private List<String> normalizeMealTypes(List<String> values) {
        List<String> allowed = List.of("BREAKFAST", "LUNCH", "DINNER", "SNACK");
        List<String> result = values.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(allowed::contains)
                .distinct()
                .toList();
        return result.isEmpty() ? List.of("DINNER") : result;
    }

    private String normalizeDifficulty(String value) {
        String normalized = value == null ? "EASY" : value.trim().toUpperCase();
        return List.of("EASY", "MEDIUM", "HARD").contains(normalized) ? normalized : "EASY";
    }

    private double number(JsonNode node, String field, double fallback) {
        return node == null || !node.has(field) ? fallback : node.path(field).asDouble(fallback);
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String trim(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
