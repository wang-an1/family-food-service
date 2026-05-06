package com.familyfood.ai.provider;

import com.familyfood.common.AppException;
import com.familyfood.config.AppProperties;
import com.familyfood.system.api.SystemConfigApi;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class RoutingAiProvider implements AiProvider {
    private final AppProperties properties;
    private final SystemConfigApi configService;
    private final DeepSeekAiProvider deepSeekProvider;
    private final MockAiProvider mockProvider;

    public RoutingAiProvider(AppProperties properties, SystemConfigApi configService,
                             DeepSeekAiProvider deepSeekProvider, MockAiProvider mockProvider) {
        this.properties = properties;
        this.configService = configService;
        this.deepSeekProvider = deepSeekProvider;
        this.mockProvider = mockProvider;
    }

    @Override
    public String modelName() {
        return delegate().modelName();
    }

    @Override
    public AiStructuredResult extractDishes(AiExtractionRequest request) {
        return delegate().extractDishes(request);
    }

    @Override
    public AiRecommendationResult recommend(AiRecommendationRequest request) {
        return delegate().recommend(request);
    }

    @Override
    public AiMenuPlanResult planMenu(AiMenuPlanRequest request) {
        return delegate().planMenu(request);
    }

    @Override
    public String summarize(AiSummarizeRequest request) {
        return delegate().summarize(request);
    }

    private AiProvider delegate() {
        if (!configService.bool("ai.enabled", true)) {
            throw AppException.badRequest("AI 功能已关闭");
        }
        String provider = configService.value("ai.provider", properties.aiProvider());
        if (provider == null || provider.isBlank() || "mock".equalsIgnoreCase(provider)) {
            return mockProvider;
        }
        if ("deepseek".equalsIgnoreCase(provider) || "deepseek-v4".equalsIgnoreCase(provider)
                || "openai-compatible".equalsIgnoreCase(provider)) {
            return deepSeekProvider.isConfigured() ? deepSeekProvider : mockProvider;
        }
        throw AppException.badRequest("不支持的 AI Provider：" + provider);
    }
}
