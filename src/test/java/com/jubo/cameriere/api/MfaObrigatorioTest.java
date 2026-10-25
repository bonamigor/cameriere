package com.jubo.cameriere.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.*;

@DisplayName("API 2FA obrigatório para administradores")
@TestPropertySource(properties = "cameriere.security.mfa.obrigatorio-para-admin=true")
class MfaObrigatorioTest extends ApiAuthSupport {

    @Test
    @DisplayName("Admin sem 2FA é bloqueado no restante da API, mas consegue configurá-lo")
    void quandoAdminSemMfa_deveBloquearExcetoConfiguracao() {
        givenAdmin().when().get("/api/clientes")
                .then().statusCode(HttpStatus.FORBIDDEN.value())
                .body("type", endsWith("/mfa-obrigatorio"));

        givenAdmin().when().get("/api/auth/me")
                .then().statusCode(200)
                .body("doisFatoresAtivo", equalTo(false));

        givenAdmin().when().post("/api/auth/2fa/configurar")
                .then().statusCode(200)
                .body("segredo", notNullValue());
    }

    @Test
    @DisplayName("Usuário comum (escola) não é afetado pela obrigatoriedade")
    void quandoUsuarioComum_naoDeveExigirMfa() {
        givenUser().when().get("/api/auth/me").then().statusCode(200);
        givenUser().when().get("/api/clientes").then().statusCode(not(HttpStatus.FORBIDDEN.value()));
    }
}
