package com.jubo.cameriere.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cameriere.security.jwt")
public record JwtProperties(
        // HS256 exige chave de pelo menos 256 bits
        @NotBlank @Size(min = 32, message = "JWT_SECRET deve ter pelo menos 32 caracteres")
        String secret,

        @Positive
        long expirationMs
) {
}
