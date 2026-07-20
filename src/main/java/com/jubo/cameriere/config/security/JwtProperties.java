package com.jubo.cameriere.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cameriere.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {
}
