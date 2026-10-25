package com.jubo.cameriere.config.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cameriere.security.mfa")
public record MfaProperties(
        /** Nome exibido no aplicativo autenticador (Google Authenticator, Authy...). */
        @NotBlank
        String emissor,

        /** Administradores sem 2FA só acessam as rotas de configuração do 2FA. */
        boolean obrigatorioParaAdmin,

        /** Chave AES-256 em Base64 (32 bytes) que protege os segredos TOTP no banco. */
        @NotBlank
        String chaveCriptografia
) {
}
