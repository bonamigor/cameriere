package com.jubo.cameriere.config.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cameriere.security.cookie")
public record AuthCookieProperties(
        @NotBlank
        String name,

        boolean secure,

        @NotBlank
        String sameSite
) {
}
