package com.familyfood.common.secret;

import com.familyfood.config.AppProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretCryptoServiceTests {

    @Test
    void encryptsWithoutPlaintextAndDecryptsWithConfiguredKey() {
        SecretCryptoService service = service("test-v1", key("0123456789abcdef0123456789abcdef"));

        String encrypted = service.encrypt("plain-test-key");

        assertTrue(encrypted.startsWith(SecretCryptoService.CIPHERTEXT_PREFIX + "test-v1:"));
        assertTrue(!encrypted.contains("plain-test-key"));
        assertEquals("plain-test-key", service.decrypt(encrypted));
    }

    @Test
    void encryptingSamePlaintextProducesDifferentCiphertext() {
        SecretCryptoService service = service("test-v1", key("0123456789abcdef0123456789abcdef"));

        String first = service.encrypt("plain-test-key");
        String second = service.encrypt("plain-test-key");

        assertNotEquals(first, second);
        assertEquals("plain-test-key", service.decrypt(first));
        assertEquals("plain-test-key", service.decrypt(second));
    }

    @Test
    void decryptFailsWithWrongKey() {
        SecretCryptoService service = service("test-v1", key("0123456789abcdef0123456789abcdef"));
        SecretCryptoService wrongKeyService = service("test-v1", key("abcdef0123456789abcdef0123456789"));
        String encrypted = service.encrypt("plain-test-key");

        assertThrows(IllegalStateException.class, () -> wrongKeyService.decrypt(encrypted));
    }

    @Test
    void decryptFailsWhenCiphertextIsTampered() {
        SecretCryptoService service = service("test-v1", key("0123456789abcdef0123456789abcdef"));
        String encrypted = service.encrypt("plain-test-key");
        String tampered = encrypted.substring(0, encrypted.length() - 1)
                + (encrypted.endsWith("A") ? "B" : "A");

        assertThrows(RuntimeException.class, () -> service.decrypt(tampered));
    }

    private SecretCryptoService service(String keyId, String key) {
        return new SecretCryptoService(new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                "http://localhost:5173",
                "mock",
                new AppProperties.Ai("mock-chat", 30),
                new AppProperties.Secret(keyId, key)
        ));
    }

    private String key(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
