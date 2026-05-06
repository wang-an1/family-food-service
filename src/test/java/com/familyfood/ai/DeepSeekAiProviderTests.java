package com.familyfood.ai;

import com.familyfood.ai.provider.AiProvider;
import com.familyfood.ai.provider.DeepSeekAiProvider;
import com.familyfood.config.AppProperties;
import com.familyfood.system.service.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeepSeekAiProviderTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        DeepSeekAiProvider provider = new DeepSeekAiProvider(properties(), configService(), objectMapper);

        AiProvider.AiStructuredResult result = provider.extractDishes(new AiProvider.AiExtractionRequest(
                "TEXT", "鸡翅", "", "空气炸锅鸡翅", "", List.of()
        ));

        assertEquals("deepseek-v4", provider.modelName());
        assertEquals("识别到空气炸锅鸡翅", result.summary());
        assertEquals("空气炸锅鸡翅", result.dishes().get(0).name());
        assertEquals("鸡翅", result.dishes().get(0).ingredients().get(0).name());
    }

    private void startServer(String content) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            exchange.getRequestBody().readAllBytes();
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

    private SystemConfigService configService() {
        SystemConfigService configService = mock(SystemConfigService.class);
        when(configService.value(eq("ai.base_url"), anyString())).thenReturn("http://localhost:" + server.getAddress().getPort());
        when(configService.value(eq("ai.api_key"), anyString())).thenReturn("test-key");
        when(configService.value(eq("ai.chat_model"), anyString())).thenReturn("deepseek-v4");
        return configService;
    }

    private AppProperties properties() {
        return new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                "http://localhost:5173",
                "deepseek",
                new AppProperties.Ai("http://localhost:" + server.getAddress().getPort(), "test-key", "deepseek-v4", 30)
        );
    }
}
