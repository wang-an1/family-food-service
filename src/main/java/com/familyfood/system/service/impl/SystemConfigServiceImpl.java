package com.familyfood.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.common.secret.SecretCryptoService;
import com.familyfood.system.dao.SystemConfigMapper;
import com.familyfood.system.dto.ConfigItem;
import com.familyfood.system.dto.ConfigResponse;
import com.familyfood.system.dto.UpdateRequest;
import com.familyfood.system.entity.SystemConfig;
import com.familyfood.system.service.SystemConfigService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SystemConfigServiceImpl implements SystemConfigService {
    private final SystemConfigMapper configMapper;
    private final ActorContextProvider actorProvider;
    private final AiCatalogService aiCatalogService;
    private final SecretCryptoService secretCryptoService;

    @Autowired
    public SystemConfigServiceImpl(SystemConfigMapper configMapper, ActorContextProvider actorProvider,
                                   AiCatalogService aiCatalogService, SecretCryptoService secretCryptoService) {
        this.configMapper = configMapper;
        this.actorProvider = actorProvider;
        this.aiCatalogService = aiCatalogService;
        this.secretCryptoService = secretCryptoService;
    }

    public List<ConfigResponse> list() {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        return configMapper.selectList(new QueryWrapper<SystemConfig>()
                        .eq("family_id", actor.familyId())
                        .orderByAsc("config_key"))
                .stream().map(this::mask).toList();
    }

    @Transactional
    public List<ConfigResponse> update(UpdateRequest request) {
        ActorContext actor = actorProvider.current();
        actor.requireAdmin();
        validateAiSelection(actor, request);
        for (ConfigItem item : request.configs()) {
            SystemConfig config = configMapper.selectOne(new QueryWrapper<SystemConfig>()
                    .eq("family_id", actor.familyId())
                    .eq("config_key", item.key())
                    .last("limit 1"));
            if (config == null) {
                config = new SystemConfig();
                config.setFamilyId(actor.familyId());
                config.setConfigKey(item.key());
            }
            boolean secret = isSecretKey(item.key());
            if (secret) {
                if (item.value() != null && !item.value().isBlank()) {
                    config.setConfigValue(encryptSecret(item.value().trim()));
                } else if (config.getId() == null) {
                    config.setConfigValue(null);
                }
            } else {
                config.setConfigValue(item.value());
            }
            config.setValueType(item.valueType() == null ? "STRING" : item.valueType());
            config.setEncrypted(secret ? 1 : 0);
            config.setUpdatedAt(LocalDateTime.now());
            if (config.getId() == null) {
                configMapper.insert(config);
            } else {
                configMapper.updateById(config);
            }
        }
        return list();
    }

    @Override
    public boolean bool(String key, boolean defaultValue) {
        SystemConfig config = find(key);
        if (config == null) {
            return defaultValue;
        }
        return Objects.equals("true", config.getConfigValue());
    }

    @Override
    public String value(String key, String defaultValue) {
        if (isSecretKey(key)) {
            return defaultValue;
        }
        SystemConfig config = find(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }
        return config.getConfigValue();
    }

    @Override
    public String secretValue(String key, String defaultValue) {
        if (!isSecretKey(key)) {
            return value(key, defaultValue);
        }
        SystemConfig config = find(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaultValue;
        }
        try {
            return secretCryptoService.decrypt(config.getConfigValue());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw AppException.serviceUnavailable("SECRET_CONFIG_ERROR", "系统密钥配置异常，暂时无法读取密钥，请联系管理员检查配置");
        }
    }

    private SystemConfig find(String key) {
        ActorContext actor = actorProvider.current();
        return configMapper.selectOne(new QueryWrapper<SystemConfig>()
                .eq("family_id", actor.familyId())
                .eq("config_key", key)
                .last("limit 1"));
    }

    private ConfigResponse mask(SystemConfig config) {
        if (config.getEncrypted() != null && config.getEncrypted() == 1) {
            boolean configured = config.getConfigValue() != null && !config.getConfigValue().isBlank();
            return new ConfigResponse(config.getConfigKey(), null, config.getValueType(), configured);
        }
        return new ConfigResponse(config.getConfigKey(), config.getConfigValue(), config.getValueType(), false);
    }

    private boolean isSecretKey(String key) {
        return key != null && key.contains("api_key");
    }

    private String encryptSecret(String value) {
        try {
            return secretCryptoService.encrypt(value);
        } catch (IllegalStateException ex) {
            throw AppException.validation("系统密钥配置缺失或无效，请先补齐配置");
        }
    }

    private void validateAiSelection(ActorContext actor, UpdateRequest request) {
        boolean touchesAiSelection = request.configs().stream()
                .map(ConfigItem::key)
                .anyMatch(key -> "ai.provider".equals(key) || "ai.chat_model".equals(key));
        if (!touchesAiSelection) {
            return;
        }
        Map<String, String> nextValues = new HashMap<>();
        configMapper.selectList(new QueryWrapper<SystemConfig>().eq("family_id", actor.familyId()))
                .forEach(config -> nextValues.put(config.getConfigKey(), config.getConfigValue()));
        for (ConfigItem item : request.configs()) {
            nextValues.put(item.key(), item.value());
        }
        aiCatalogService.validateSelection(nextValues.get("ai.provider"), nextValues.get("ai.chat_model"));
    }
}
