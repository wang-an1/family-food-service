package com.familyfood.bootstrap;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.familyfood.common.secret.SecretCryptoService;
import com.familyfood.config.AppProperties;
import com.familyfood.system.dao.SystemConfigMapper;
import com.familyfood.system.entity.SystemConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiApiKeyEncryptionMigratorTests {

    @Test
    void migratesPlaintextApiKeyToEncryptedValue() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        SystemConfig config = config("plain-test-key", 1);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of(config));
        SecretCryptoService secretCryptoService = secretCryptoService();
        AiApiKeyEncryptionMigrator migrator = new AiApiKeyEncryptionMigrator(configMapper, secretCryptoService);

        migrator.run(null);

        ArgumentCaptor<SystemConfig> captor = ArgumentCaptor.forClass(SystemConfig.class);
        verify(configMapper).updateById(captor.capture());
        SystemConfig updated = captor.getValue();
        assertEquals(1, updated.getEncrypted());
        assertTrue(secretCryptoService.isEncrypted(updated.getConfigValue()));
        assertEquals("plain-test-key", secretCryptoService.decrypt(updated.getConfigValue()));
    }

    @Test
    void leavesAlreadyEncryptedApiKeyUntouched() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        String encrypted = secretCryptoService().encrypt("plain-test-key");
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of(config(encrypted, 1)));
        AiApiKeyEncryptionMigrator migrator = new AiApiKeyEncryptionMigrator(configMapper, secretCryptoService());

        migrator.run(null);

        verify(configMapper, never()).updateById(org.mockito.ArgumentMatchers.<SystemConfig>any());
    }

    @Test
    void failsWhenPlaintextApiKeyExistsWithoutSecretConfiguration() {
        SystemConfigMapper configMapper = mock(SystemConfigMapper.class);
        when(configMapper.selectList(any(Wrapper.class))).thenReturn(List.of(config("plain-test-key", 1)));
        AiApiKeyEncryptionMigrator migrator = new AiApiKeyEncryptionMigrator(configMapper, unconfiguredSecretCryptoService());

        assertThrows(IllegalStateException.class, () -> migrator.run(null));
    }

    private SystemConfig config(String value, int encrypted) {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey("ai.api_key");
        config.setConfigValue(value);
        config.setValueType("STRING");
        config.setEncrypted(encrypted);
        return config;
    }

    private SecretCryptoService secretCryptoService() {
        return new SecretCryptoService(properties(new AppProperties.Secret("test-v1", Base64.getEncoder()
                .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)))));
    }

    private SecretCryptoService unconfiguredSecretCryptoService() {
        return new SecretCryptoService(properties(null));
    }

    private AppProperties properties(AppProperties.Secret secret) {
        return new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                "http://localhost:5173",
                "mock",
                new AppProperties.Ai("mock-chat", 30),
                secret
        );
    }
}
