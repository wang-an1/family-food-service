package com.familyfood.ai.provider;

import com.familyfood.ai.entity.AiProviderCatalog;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.common.AppException;
import com.familyfood.config.AppProperties;
import com.familyfood.system.api.SystemConfigApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class RoutingAiProvider implements AiProvider {
    private final AppProperties properties;
    private final SystemConfigApi configService;
    private final AiCatalogService catalogService;
    private final DeepSeekAiProvider deepSeekProvider;
    private final MockAiProvider mockProvider;

    @Autowired
    public RoutingAiProvider(AppProperties properties, SystemConfigApi configService,
                             AiCatalogService catalogService, DeepSeekAiProvider deepSeekProvider,
                             MockAiProvider mockProvider) {
        this.properties = properties;
        this.configService = configService;
        this.catalogService = catalogService;
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
            throw AppException.badRequest("AI 功能当前已关闭，请先在系统配置中开启");
        }
        AiProviderCatalog provider = catalogService.requireActiveProvider(configService.value("ai.provider", properties.aiProvider()));
        if (AiCatalogService.CALL_TYPE_MOCK.equals(provider.getCallType())) {
            return mockProvider;
        }
        if (AiCatalogService.CALL_TYPE_OPENAI_CHAT_COMPLETIONS.equals(provider.getCallType())) {
            return deepSeekProvider.isConfigured() ? deepSeekProvider : mockProvider;
        }
        throw AppException.badRequest("当前选择的 AI 服务暂不支持，请检查系统配置");
    }
}
