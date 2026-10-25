package com.jubo.cameriere.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@DisplayName("API Auth")
class AuthTest extends ApiAuthSupport {

    @Test
    @DisplayName("Login com credenciais válidas retorna token JWT")
    void quandoLoginValido_deveRetornarToken() {
        givenSemAuth()
                .body("""
                        {
                          "email": "%s",
                          "senha": "%s"
                        }
                        """.formatted(ADMIN_EMAIL, SENHA_PADRAO))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("email", equalTo(ADMIN_EMAIL))
                .body("role", equalTo("ADMIN"))
                .body("expiresInMs", greaterThan(0));
    }

    @Test
    @DisplayName("Login define cookie HttpOnly que autentica as requisições seguintes")
    void quandoLoginValido_deveDefinirCookieHttpOnly() {
        String setCookie = givenSemAuth()
                .body("""
                        {
                          "email": "%s",
                          "senha": "%s"
                        }
                        """.formatted(ADMIN_EMAIL, SENHA_PADRAO))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .header("Set-Cookie", allOf(
                        startsWith("cameriere_token="),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax")))
                .extract()
                .header("Set-Cookie");

        String token = setCookie.substring("cameriere_token=".length(), setCookie.indexOf(';'));

        givenSemAuth()
                .cookie("cameriere_token", token)
                .when()
                .get("/api/clientes")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Logout expira o cookie de autenticação")
    void quandoLogout_deveExpirarCookie() {
        givenSemAuth()
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .header("Set-Cookie", allOf(
                        startsWith("cameriere_token=;"),
                        containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("Login com senha inválida retorna 401")
    void quandoLoginComSenhaInvalida_deveRetornar401() {
        givenSemAuth()
                .body("""
                        {
                          "email": "%s",
                          "senha": "senha-errada"
                        }
                        """.formatted(ADMIN_EMAIL))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Acesso sem token retorna 401")
    void quandoAcessarSemToken_deveRetornar401() {
        givenSemAuth()
                .when()
                .get("/api/clientes")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("USER não pode criar cliente (403)")
    void quandoUserCriarCliente_deveRetornar403() {
        givenUser()
                .body("""
                        {
                          "nome": "Cliente Bloqueado",
                          "cnpj": "%s",
                          "endereco": { "cep": "01310-100", "estado": "SP" }
                        }
                        """.formatted(cnpjUnico()))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .body("title", equalTo("Acesso negado"));
    }

    @Test
    @DisplayName("USER pode listar produtos (200)")
    void quandoUserListarProdutos_deveRetornar200() {
        givenUser()
                .when()
                .get("/api/produtos")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("USER não pode criar usuário (403)")
    void quandoUserCriarUsuario_deveRetornar403() {
        givenUser()
                .body("""
                        {
                          "nome": "Novo User",
                          "email": "%s",
                          "senha": "Senha@2026",
                          "admin": false
                        }
                        """.formatted(emailUnico("bloqueado")))
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
