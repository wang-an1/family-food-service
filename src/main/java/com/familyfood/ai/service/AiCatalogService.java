package com.familyfood.ai.service;

import com.familyfood.ai.dto.AiModelCatalogResponse;
import com.familyfood.ai.dto.AiModelRequest;
import com.familyfood.ai.dto.AiProviderCatalogResponse;
import com.familyfood.ai.dto.AiProviderCreateRequest;
import com.familyfood.ai.dto.AiProviderUpdateRequest;
import com.familyfood.ai.entity.AiModelCatalog;
import com.familyfood.ai.entity.AiProviderCatalog;
import java.util.List;

public interface AiCatalogService {
    String CALL_TYPE_OPENAI_CHAT_COMPLETIONS = "OPENAI_CHAT_COMPLETIONS";
    String CALL_TYPE_MOCK = "MOCK";
    String STATUS_ACTIVE = "ACTIVE";
    String STATUS_INACTIVE = "INACTIVE";

    List<AiProviderCatalogResponse> listProviders();

    AiProviderCatalogResponse createProvider(AiProviderCreateRequest request);

    AiProviderCatalogResponse updateProvider(Long id, AiProviderUpdateRequest request);

    AiModelCatalogResponse createModel(Long providerId, AiModelRequest request);

    AiModelCatalogResponse updateModel(Long id, AiModelRequest request);

    AiProviderCatalog requireActiveProvider(String code);

    AiModelCatalog requireActiveModel(String providerCode, String modelName);

    void validateSelection(String providerCode, String modelName);
}
