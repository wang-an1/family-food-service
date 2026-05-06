package com.familyfood.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "family-food")
public record AppProperties(
        @Valid @NotNull
        Jwt jwt,
        @NotBlank
        String uploadDir,
        @NotBlank
        String initAdminPassword,
        @NotBlank
        String corsAllowedOrigins,
        @NotBlank
        String aiProvider,
        @Valid @NotNull
        Ai ai
) {
    public record Jwt(
            @NotBlank @Size(min = 32)
            String secret,
            @Min(5)
            long ttlMinutes
    ) {
    }

    public record Ai(
            @NotBlank
            String baseUrl,
            String apiKey,
            @NotBlank
            String chatModel,
            @Min(1)
            int timeoutSeconds
    ) {
    }

}
