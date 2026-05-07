package com.familyfood.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.common.secret.SecretCryptoService;
import com.familyfood.system.dao.SystemConfigMapper;
import com.familyfood.system.entity.SystemConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AiApiKeyEncryptionMigrator implements ApplicationRunner {
    private static final String AI_API_KEY = "ai.api_key";

    private final SystemConfigMapper configMapper;
    private final SecretCryptoService secretCryptoService;

    @Autowired
    public AiApiKeyEncryptionMigrator(SystemConfigMapper configMapper, SecretCryptoService secretCryptoService) {
        this.configMapper = configMapper;
        this.secretCryptoService = secretCryptoService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<SystemConfig> configs = configMapper.selectList(new QueryWrapper<SystemConfig>()
                .eq("config_key", AI_API_KEY));
        for (SystemConfig config : configs) {
            migrate(config);
        }
    }

    private void migrate(SystemConfig config) {
        String value = config.getConfigValue();
        if (value == null || value.isBlank()) {
            if (config.getEncrypted() == null || config.getEncrypted() != 1) {
                markEncrypted(config);
            }
            return;
        }
        if (secretCryptoService.isEncrypted(value)) {
            if (!secretCryptoService.isConfigured()) {
                throw new IllegalStateException("AI 密钥已加密，但缺少密钥配置，请先补充 family-food.secret 配置");
            }
            secretCryptoService.decrypt(value);
            if (config.getEncrypted() == null || config.getEncrypted() != 1) {
                markEncrypted(config);
            }
            return;
        }
        if (!secretCryptoService.isConfigured()) {
            throw new IllegalStateException("检测到未加密的 AI 密钥，但缺少密钥配置，暂时无法完成迁移");
        }
        config.setConfigValue(secretCryptoService.encrypt(value));
        config.setEncrypted(1);
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.updateById(config);
    }

    private void markEncrypted(SystemConfig config) {
        config.setEncrypted(1);
        config.setUpdatedAt(LocalDateTime.now());
        configMapper.updateById(config);
    }
}
