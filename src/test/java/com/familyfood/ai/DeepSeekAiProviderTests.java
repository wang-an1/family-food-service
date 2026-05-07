package com.familyfood.ai;

import com.familyfood.ai.entity.AiModelCatalog;
import com.familyfood.ai.entity.AiProviderCatalog;
import com.familyfood.ai.provider.AiProvider;
import com.familyfood.ai.provider.DeepSeekAiProvider;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.config.AppProperties;
import com.familyfood.system.service.SystemConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeepSeekAiProviderTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<JsonNode> requestBody = new AtomicReference<>();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void extractDishesUsesDeepSeekChatCompletionsShape() throws Exception {
        String modelResponse = String.join("\n",
                "{",
                "  \"summary\": \"识别到空气炸锅鸡翅\",",
                "  \"dishes\": [",
                "    {",
                "      \"name\": \"空气炸锅鸡翅\",",
                "      \"tags\": [\"快手菜\"],",
                "      \"taste\": \"咸香\",",
                "      \"mealTypes\": [\"DINNER\"],",
                "      \"difficulty\": \"EASY\",",
                "      \"estimatedMinutes\": 25,",
                "      \"ingredients\": [{\"name\": \"鸡翅\", \"amount\": 500, \"unit\": \"克\", \"category\": \"肉类\", \"required\": true}],",
                "      \"instructions\": \"腌制后空气炸锅烤熟。\",",
                "      \"reason\": \"适合家庭晚餐\",",
                "      \"confidence\": 0.9",
                "    }",
                "  ]",
                "}"
        );
        startServer(modelResponse);
        DeepSeekAiProvider provider = new DeepSeekAiProvider(properties("deepseek"), configService("deepseek", "deepseek-v4-flash"),
                catalogService("deepseek", "deepseek-v4-flash"), objectMapper);

        AiProvider.AiStructuredResult result = provider.extractDishes(new AiProvider.AiExtractionRequest(
                "TEXT", "鸡翅", "", "空气炸锅鸡翅", "", List.of()
        ));

        assertEquals("deepseek-v4-flash", provider.modelName());
        assertEquals("deepseek-v4-flash", requestBody.get().path("model").asText());
        assertEquals("识别到空气炸锅鸡翅", result.summary());
        assertEquals("空气炸锅鸡翅", result.dishes().get(0).name());
        assertEquals("鸡翅", result.dishes().get(0).ingredients().get(0).name());
    }

    @Test
    void usesProviderCatalogBaseUrlAndConfiguredModel() throws Exception {
        startServer("{\"summary\":\"ok\",\"dishes\":[]}");
        DeepSeekAiProvider provider = new DeepSeekAiProvider(properties("openai-compatible"),
                configService("openai-compatible", "gpt-5.4"),
                catalogService("openai-compatible", "gpt-5.4"), objectMapper);

        provider.extractDishes(new AiProvider.AiExtractionRequest("TEXT", "test", "", "test", "", List.of()));

        assertEquals("gpt-5.4", provider.modelName());
        assertEquals("gpt-5.4", requestBody.get().path("model").asText());
    }

    private void startServer(String content) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            requestBody.set(objectMapper.readTree(exchange.getRequestBody().readAllBytes()));
            if (!"Bearer test-key".equals(authorization)) {
                exchange.sendResponseHeaders(401, -1);
                return;
            }
            byte[] body = objectMapper.writeValueAsBytes(Map.of(
                    "choices", List.of(Map.of("message", Map.of("content", content)))
            ));
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    private SystemConfigService configService(String providerCode, String modelName) {
        SystemConfigService configService = mock(SystemConfigService.class);
        when(configService.secretValue(eq("ai.api_key"), anyString())).thenReturn("test-key");
        when(configService.value(eq("ai.provider"), anyString())).thenReturn(providerCode);
        when(configService.value(eq("ai.chat_model"), anyString())).thenReturn(modelName);
        return configService;
    }

    private AiCatalogService catalogService(String providerCode, String modelName) {
        AiProviderCatalog provider = new AiProviderCatalog();
        provider.setCode(providerCode);
        provider.setDisplayName(providerCode);
        provider.setCallType(AiCatalogService.CALL_TYPE_OPENAI_CHAT_COMPLETIONS);
        provider.setBaseUrl("http://localhost:" + server.getAddress().getPort());
        provider.setStatus(AiCatalogService.STATUS_ACTIVE);

        AiModelCatalog model = new AiModelCatalog();
        model.setProviderId(1L);
        model.setModelName(modelName);
        model.setDisplayName(modelName);
        model.setStatus(AiCatalogService.STATUS_ACTIVE);

        AiCatalogService catalogService = mock(AiCatalogService.class);
        when(catalogService.requireActiveProvider(eq(providerCode))).thenReturn(provider);
        when(catalogService.requireActiveModel(eq(providerCode), anyString())).thenReturn(model);
        return catalogService;
    }

    private AppProperties properties(String providerCode) {
        return new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                "http://localhost:5173",
                providerCode,
                new AppProperties.Ai("deepseek-v4-pro", 30),
                null
        );
    }
}
