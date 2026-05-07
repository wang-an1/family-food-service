package com.familyfood.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.ai.dao.AiModelCatalogMapper;
import com.familyfood.ai.dao.AiProviderCatalogMapper;
import com.familyfood.ai.dto.AiModelCatalogResponse;
import com.familyfood.ai.dto.AiModelRequest;
import com.familyfood.ai.dto.AiProviderCatalogResponse;
import com.familyfood.ai.dto.AiProviderCreateRequest;
import com.familyfood.ai.dto.AiProviderUpdateRequest;
import com.familyfood.ai.entity.AiModelCatalog;
import com.familyfood.ai.entity.AiProviderCatalog;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AiCatalogServiceImpl implements AiCatalogService {
    private final AiProviderCatalogMapper providerMapper;
    private final AiModelCatalogMapper modelMapper;
    private final ActorContextProvider actorProvider;

    @Autowired
    public AiCatalogServiceImpl(AiProviderCatalogMapper providerMapper, AiModelCatalogMapper modelMapper,
                                ActorContextProvider actorProvider) {
        this.providerMapper = providerMapper;
        this.modelMapper = modelMapper;
        this.actorProvider = actorProvider;
    }

    @Override
    public List<AiProviderCatalogResponse> listProviders() {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        List<AiProviderCatalog> providers = providerMapper.selectList(new QueryWrapper<AiProviderCatalog>()
                .orderByAsc("sort_order")
                .orderByAsc("id"));
        if (providers.isEmpty()) {
            return List.of();
        }
        List<Long> providerIds = providers.stream().map(AiProviderCatalog::getId).toList();
        Map<Long, List<AiModelCatalog>> models = modelMapper.selectList(new QueryWrapper<AiModelCatalog>()
                        .in("provider_id", providerIds)
                        .orderByAsc("sort_order")
                        .orderByAsc("id"))
                .stream()
                .collect(Collectors.groupingBy(AiModelCatalog::getProviderId));
        return providers.stream()
                .map(provider -> toProviderResponse(provider, models.getOrDefault(provider.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public AiProviderCatalogResponse createProvider(AiProviderCreateRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        validateProviderRequest(request.callType(), request.baseUrl());
        LocalDateTime now = LocalDateTime.now();
        AiProviderCatalog provider = new AiProviderCatalog();
        provider.setCode(normalizeProviderCode(request.code()));
        provider.setDisplayName(request.displayName().trim());
        provider.setCallType(request.callType());
        provider.setBaseUrl(normalizeOptional(request.baseUrl()));
        provider.setStatus(request.status());
        provider.setSortOrder(defaultSort(request.sortOrder()));
        provider.setCreatedAt(now);
        provider.setUpdatedAt(now);
        try {
            providerMapper.insert(provider);
        } catch (DuplicateKeyException ex) {
            throw AppException.conflict("这个供应商编码已经存在，请换一个编码");
        }
        return toProviderResponse(provider, List.of());
    }

    @Override
    @Transactional
    public AiProviderCatalogResponse updateProvider(Long id, AiProviderUpdateRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        validateProviderRequest(request.callType(), request.baseUrl());
        AiProviderCatalog provider = requireProviderById(id);
        provider.setDisplayName(request.displayName().trim());
        provider.setCallType(request.callType());
        provider.setBaseUrl(normalizeOptional(request.baseUrl()));
        provider.setStatus(request.status());
        provider.setSortOrder(defaultSort(request.sortOrder()));
        provider.setUpdatedAt(LocalDateTime.now());
        providerMapper.updateById(provider);
        return toProviderResponse(provider, modelsByProvider(provider.getId()));
    }

    @Override
    @Transactional
    public AiModelCatalogResponse createModel(Long providerId, AiModelRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        requireProviderById(providerId);
        validateModelRequest(request);
        LocalDateTime now = LocalDateTime.now();
        AiModelCatalog model = new AiModelCatalog();
        model.setProviderId(providerId);
        model.setModelName(request.modelName().trim());
        model.setDisplayName(request.displayName().trim());
        model.setDefaultModel(Boolean.TRUE.equals(request.defaultModel()) ? 1 : 0);
        model.setStatus(request.status());
        model.setSortOrder(defaultSort(request.sortOrder()));
        model.setCreatedAt(now);
        model.setUpdatedAt(now);
        if (model.getDefaultModel() == 1) {
            clearDefaultModels(providerId, null);
        }
        try {
            modelMapper.insert(model);
        } catch (DuplicateKeyException ex) {
            throw AppException.conflict("这个供应商下已经有同名模型，请换一个模型名称");
        }
        return toModelResponse(model);
    }

    @Override
    @Transactional
    public AiModelCatalogResponse updateModel(Long id, AiModelRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        validateModelRequest(request);
        AiModelCatalog model = requireModelById(id);
        model.setModelName(request.modelName().trim());
        model.setDisplayName(request.displayName().trim());
        model.setDefaultModel(Boolean.TRUE.equals(request.defaultModel()) ? 1 : 0);
        model.setStatus(request.status());
        model.setSortOrder(defaultSort(request.sortOrder()));
        model.setUpdatedAt(LocalDateTime.now());
        if (model.getDefaultModel() == 1) {
            clearDefaultModels(model.getProviderId(), model.getId());
        }
        try {
            modelMapper.updateById(model);
        } catch (DuplicateKeyException ex) {
            throw AppException.conflict("这个供应商下已经有同名模型，请换一个模型名称");
        }
        return toModelResponse(model);
    }

    @Override
    public AiProviderCatalog requireActiveProvider(String code) {
        String normalized = normalizeProviderCode(code);
        if (normalized.isBlank()) {
            normalized = "mock";
        }
        AiProviderCatalog provider = providerMapper.selectOne(new QueryWrapper<AiProviderCatalog>()
                .eq("code", normalized)
                .last("limit 1"));
        if (provider == null) {
            throw AppException.badRequest("未找到已配置的 AI 供应商，请检查系统配置");
        }
        if (!STATUS_ACTIVE.equals(provider.getStatus())) {
            throw AppException.badRequest("当前 AI 供应商已停用，请先启用或重新选择");
        }
        return provider;
    }

    @Override
    public AiModelCatalog requireActiveModel(String providerCode, String modelName) {
        AiProviderCatalog provider = requireActiveProvider(providerCode);
        if (CALL_TYPE_MOCK.equals(provider.getCallType())) {
            return null;
        }
        String normalizedModel = modelName == null ? "" : modelName.trim();
        AiModelCatalog model = normalizedModel.isBlank()
                ? defaultActiveModel(provider.getId())
                : modelMapper.selectOne(new QueryWrapper<AiModelCatalog>()
                        .eq("provider_id", provider.getId())
                        .eq("model_name", normalizedModel)
                        .last("limit 1"));
        if (model == null) {
            throw AppException.badRequest("未找到已配置的 AI 模型，请检查系统配置");
        }
        if (!STATUS_ACTIVE.equals(model.getStatus())) {
            throw AppException.badRequest("当前 AI 模型已停用，请先启用或重新选择");
        }
        return model;
    }

    @Override
    public void validateSelection(String providerCode, String modelName) {
        AiProviderCatalog provider = requireActiveProvider(providerCode);
        if (!CALL_TYPE_MOCK.equals(provider.getCallType())) {
            requireActiveModel(provider.getCode(), modelName);
        }
    }

    private void validateProviderRequest(String callType, String baseUrl) {
        if (!CALL_TYPE_MOCK.equals(callType) && normalizeOptional(baseUrl) == null) {
            throw AppException.validation("使用真实 AI 服务时，请填写接口基础地址");
        }
    }

    private void validateModelRequest(AiModelRequest request) {
        if (Boolean.TRUE.equals(request.defaultModel()) && !STATUS_ACTIVE.equals(request.status())) {
            throw AppException.validation("默认模型需要保持启用状态");
        }
    }

    private void clearDefaultModels(Long providerId, Long exceptModelId) {
        List<AiModelCatalog> models = modelMapper.selectList(new QueryWrapper<AiModelCatalog>()
                .eq("provider_id", providerId)
                .eq("default_model", 1));
        for (AiModelCatalog model : models) {
            if (exceptModelId != null && Objects.equals(model.getId(), exceptModelId)) {
                continue;
            }
            model.setDefaultModel(0);
            model.setUpdatedAt(LocalDateTime.now());
            modelMapper.updateById(model);
        }
    }

    private AiProviderCatalog requireProviderById(Long id) {
        AiProviderCatalog provider = providerMapper.selectById(id);
        if (provider == null) {
            throw AppException.notFound("未找到这个 AI 供应商");
        }
        return provider;
    }

    private AiModelCatalog requireModelById(Long id) {
        AiModelCatalog model = modelMapper.selectById(id);
        if (model == null) {
            throw AppException.notFound("未找到这个 AI 模型");
        }
        return model;
    }

    private AiModelCatalog defaultActiveModel(Long providerId) {
        AiModelCatalog model = modelMapper.selectOne(new QueryWrapper<AiModelCatalog>()
                .eq("provider_id", providerId)
                .eq("default_model", 1)
                .eq("status", STATUS_ACTIVE)
                .orderByAsc("sort_order")
                .orderByAsc("id")
                .last("limit 1"));
        if (model != null) {
            return model;
        }
        return modelMapper.selectOne(new QueryWrapper<AiModelCatalog>()
                .eq("provider_id", providerId)
                .eq("status", STATUS_ACTIVE)
                .orderByAsc("sort_order")
                .orderByAsc("id")
                .last("limit 1"));
    }

    private List<AiModelCatalog> modelsByProvider(Long providerId) {
        return modelMapper.selectList(new QueryWrapper<AiModelCatalog>()
                .eq("provider_id", providerId)
                .orderByAsc("sort_order")
                .orderByAsc("id"));
    }

    private AiProviderCatalogResponse toProviderResponse(AiProviderCatalog provider, List<AiModelCatalog> models) {
        return new AiProviderCatalogResponse(
                provider.getId(),
                provider.getCode(),
                provider.getDisplayName(),
                provider.getCallType(),
                provider.getBaseUrl(),
                provider.getStatus(),
                provider.getSortOrder(),
                models.stream().map(this::toModelResponse).toList()
        );
    }

    private AiModelCatalogResponse toModelResponse(AiModelCatalog model) {
        return new AiModelCatalogResponse(
                model.getId(),
                model.getProviderId(),
                model.getModelName(),
                model.getDisplayName(),
                model.getDefaultModel() != null && model.getDefaultModel() == 1,
                model.getStatus(),
                model.getSortOrder()
        );
    }

    private String normalizeProviderCode(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toLowerCase();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int defaultSort(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
