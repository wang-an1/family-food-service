package com.familyfood.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigCorsTests {

    @Test
    void corsConfigurationIsDisabledWhenNoOriginsAreConfigured() {
        CorsConfigurationSource source = securityConfig("").corsConfigurationSource();

        assertNull(source.getCorsConfiguration(request("http://example.com")));
    }

    @Test
    void corsConfigurationAllowsConfiguredDevelopmentOrigins() {
        CorsConfigurationSource source = securityConfig(" http://localhost:5173, http://localhost:5174 ").corsConfigurationSource();

        CorsConfiguration config = source.getCorsConfiguration(request("http://localhost:5174"));

        assertNotNull(config);
        assertEquals("http://localhost:5174", config.checkOrigin("http://localhost:5174"));
        assertNull(config.checkOrigin("http://example.com"));
    }

    private SecurityConfig securityConfig(String corsAllowedOrigins) {
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("test-family-food-secret-test-family-food-secret", 60),
                "./build/test-uploads",
                "admin123",
                corsAllowedOrigins,
                "mock",
                new AppProperties.Ai("mock-chat", 30),
                null
        );
        return new SecurityConfig(null, properties, new ObjectMapper());
    }

    private MockHttpServletRequest request(String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "POST");
        return request;
    }
}
