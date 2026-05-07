package com.familyfood.common.secret;

import com.familyfood.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecretCryptoService {
    public static final String CIPHERTEXT_PREFIX = "enc:v1:";

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int AES_256_KEY_BYTES = 32;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final AppProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public SecretCryptoService(AppProperties properties) {
        this.properties = properties;
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(CIPHERTEXT_PREFIX);
    }

    public boolean isConfigured() {
        AppProperties.Secret secret = properties.secret();
        return secret != null
                && secret.keyId() != null
                && !secret.keyId().isBlank()
                && secret.masterKeyBase64() != null
                && !secret.masterKeyBase64().isBlank();
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        SecretMaterial material = secretMaterial();
        byte[] iv = new byte[GCM_IV_BYTES];
        secureRandom.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(material.key(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return CIPHERTEXT_PREFIX + material.keyId() + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("密钥加密失败，请检查密钥配置后再试", ex);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return "";
        }
        if (!isEncrypted(ciphertext)) {
            throw new IllegalArgumentException("当前内容尚未加密，不能进行解密");
        }
        SecretMaterial material = secretMaterial();
        String[] parts = ciphertext.split(":", 4);
        if (parts.length != 4 || parts[2].isBlank() || parts[3].isBlank()) {
            throw new IllegalArgumentException("加密内容格式不正确，无法解密");
        }
        if (!material.keyId().equals(parts[2])) {
            throw new IllegalStateException("加密内容的密钥标识与当前配置不一致");
        }
        byte[] payload = Base64.getUrlDecoder().decode(parts[3]);
        if (payload.length <= GCM_IV_BYTES) {
            throw new IllegalArgumentException("加密内容不完整，无法解密");
        }
        byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_BYTES);
        byte[] encrypted = Arrays.copyOfRange(payload, GCM_IV_BYTES, payload.length);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(material.key(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("密钥解密失败，请检查密钥配置或加密内容", ex);
        }
    }

    private SecretMaterial secretMaterial() {
        AppProperties.Secret secret = properties.secret();
        if (secret == null || secret.keyId() == null || secret.keyId().isBlank()
                || secret.masterKeyBase64() == null || secret.masterKeyBase64().isBlank()) {
            throw new IllegalStateException("缺少 family-food.secret 配置，请先补充密钥标识和主密钥");
        }
        String keyId = secret.keyId().trim();
        if (keyId.contains(":")) {
            throw new IllegalStateException("family-food.secret.key-id 不能包含冒号");
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(secret.masterKeyBase64().trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("family-food.secret.master-key-base64 需要填写有效的 Base64 内容", ex);
        }
        if (key.length != AES_256_KEY_BYTES) {
            throw new IllegalStateException("family-food.secret.master-key-base64 必须是 32 字节的 AES-256 密钥");
        }
        return new SecretMaterial(keyId, key);
    }

    private record SecretMaterial(String keyId, byte[] key) {
    }
}
