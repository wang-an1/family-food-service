package com.familyfood.system;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.familyfood.ai.service.AiCatalogService;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.context.ActorContextProvider;
import com.familyfood.common.secret.SecretCryptoService;
import com.familyfood.config.AppProperties;
import com.familyfood.system.dao.SystemConfigMapper;
import com.familyfood.system.dto.ConfigItem;
import com.familyfood.system.dto.ConfigResponse;
import com.familyfood.system.dto.UpdateRequest;
import com.familyfood.system.entity.SystemConfig;
import com.familyfood.system.service.impl.SystemConfigServiceImpl;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemConfigServiceImplTests {
    private static final ActorContext ADMIN = new ActorContext(1L, 1L, "ADMIN", true);

    @Test
    void validatesAiProviderAndModelSelectionWhenSaved() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        SystemConfigServiceImpl service = service(configMapper, actorProvider, catalogService);
        service.update(new UpdateRequest(List.of(
                new ConfigItem("ai.provider", "deepseek", "STRING"),
                new ConfigItem("ai.chat_model", "deepseek-v4-flash", "STRING")
        )));

        verify(catalogService).validateSelection("deepseek", "deepseek-v4-flash");
    }

    @Test
    void skipsAiSelectionValidationForUnrelatedConfigUpdates() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of(existingConfig()));
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        SystemConfigServiceImpl service = service(configMapper, actorProvider, catalogService);
        service.update(new UpdateRequest(List.of(
                new ConfigItem("order.confirm_required", "true", "BOOLEAN")
        )));

        verify(catalogService, never()).validateSelection(any(), any());
    }

    @Test
    void savesAiApiKeyEncrypted() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        SecretCryptoService secretCryptoService = secretCryptoService();
        SystemConfigServiceImpl service = new SystemConfigServiceImpl(configMapper, actorProvider, catalogService, secretCryptoService);

        service.update(new UpdateRequest(List.of(new ConfigItem("ai.api_key", "plain-test-key", "STRING"))));

        ArgumentCaptor<SystemConfig> captor = ArgumentCaptor.forClass(SystemConfig.class);
        verify(configMapper).insert(captor.capture());
        SystemConfig saved = captor.getValue();
        assertEquals("ai.api_key", saved.getConfigKey());
        assertEquals(1, saved.getEncrypted());
        assertTrue(secretCryptoService.isEncrypted(saved.getConfigValue()));
        assertNotEquals("plain-test-key", saved.getConfigValue());
        assertEquals("plain-test-key", secretCryptoService.decrypt(saved.getConfigValue()));
    }

    @Test
    void blankAiApiKeyDoesNotOverwriteExistingSecret() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        SecretCryptoService secretCryptoService = secretCryptoService();
        String encrypted = secretCryptoService.encrypt("existing-key");
        SystemConfig existing = existingSecret(encrypted);
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        SystemConfigServiceImpl service = new SystemConfigServiceImpl(configMapper, actorProvider, catalogService, secretCryptoService);

        service.update(new UpdateRequest(List.of(new ConfigItem("ai.api_key", "", "STRING"))));

        ArgumentCaptor<SystemConfig> captor = ArgumentCaptor.forClass(SystemConfig.class);
        verify(configMapper).updateById(captor.capture());
        assertEquals(encrypted, captor.getValue().getConfigValue());
    }

    @Test
    void listMasksEncryptedSecret() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of(existingSecret("enc:v1:test:payload")));
        SystemConfigServiceImpl service = service(configMapper, actorProvider, catalogService);

        List<ConfigResponse> configs = service.list();

        assertEquals(1, configs.size());
        assertNull(configs.get(0).value());
        assertTrue(configs.get(0).configured());
    }

    @Test
    void valueDoesNotExposeSecretButSecretValueDecryptsIt() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        ActorContextProvider actorProvider = mock(ActorContextProvider.class);
        AiCatalogService catalogService = mock(AiCatalogService.class);
        SecretCryptoService secretCryptoService = secretCryptoService();
        when(actorProvider.current()).thenReturn(ADMIN);
        when(configMapper.selectOne(any(Wrapper.class))).thenReturn(existingSecret(secretCryptoService.encrypt("plain-test-key")));
        SystemConfigServiceImpl service = new SystemConfigServiceImpl(configMapper, actorProvider, catalogService, secretCryptoService);

        assertEquals("fallback", service.value("ai.api_key", "fallback"));
        assertEquals("plain-test-key", service.secretValue("ai.api_key", "fallback"));
    }

    private SystemConfig existingConfig() {
        SystemConfig config = new SystemConfig();
        config.setConfigKey("order.confirm_required");
        config.setConfigValue("false");
        config.setValueType("BOOLEAN");
        config.setEncrypted(0);
        return config;
    }

    private SystemConfig existingSecret(String value) {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey("ai.api_key");
        config.setConfigValue(value);
        config.setValueType("STRING");
        config.setEncrypted(1);
        return config;
    }

    private SystemConfigServiceImpl service(SystemConfigMapper configMapper, ActorContextProvider actorProvider,
                                            AiCatalogService catalogService) {
        return new SystemConfigServiceImpl(configMapper, actorProvider, catalogService, secretCryptoService());
    }

    private SecretCryptoService secretCryptoService() {
        return new SecretCryptoService(new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                "http://localhost:5173",
                "mock",
                new AppProperties.Ai("mock-chat", 30),
                new AppProperties.Secret("test-v1", Base64.getEncoder()
                        .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)))
        ));
    }
}
