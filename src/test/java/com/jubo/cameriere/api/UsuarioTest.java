package com.jubo.cameriere.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@DisplayName("API Usuários")
class UsuarioTest extends ApiAuthSupport {

    @Test
    @DisplayName("Criar usuário válido retorna 201 sem expor senha")
    void quandoCriarUsuarioComDadosValidos_deveRetornar201() {
        String email = emailUnico("joao");

        givenAdmin()
                .body("""
                        {
                            "nome": "João Silva",
                            "email": "%s",
                            "senha": "123456",
                            "admin": false
                        }
                        """.formatted(email))
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("João Silva"))
                .body("email", equalTo(email))
                .body("admin", equalTo(false))
                .body("senha", nullValue())
                .header("Location", containsString("/api/usuarios/"));
    }

    @Test
    @DisplayName("Criar usuário com email inválido retorna 400")
    void quandoCriarUsuarioComEmailInvalido_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "João Silva",
                            "email": "email-invalido",
                            "senha": "123456",
                            "admin": false
                        }
                        """)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("title", equalTo("Dados inválidos"))
                .body("objects.find { it.name == 'email' }.userMessage", equalTo("Email deve ser válido"));
    }

    @Test
    @DisplayName("Criar usuário com senha curta retorna 400")
    void quandoCriarUsuarioComSenhaCurta_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "João Silva",
                            "email": "%s",
                            "senha": "123",
                            "admin": false
                        }
                        """.formatted(emailUnico("senha")))
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'senha' }.userMessage",
                        equalTo("Senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    @DisplayName("Criar usuário sem nome retorna 400")
    void quandoCriarUsuarioSemNome_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "email": "%s",
                            "senha": "123456",
                            "admin": false
                        }
                        """.formatted(emailUnico("semnome")))
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }

    @Test
    @DisplayName("Criar usuário sem admin retorna 400")
    void quandoCriarUsuarioSemAdmin_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "João Silva",
                            "email": "%s",
                            "senha": "123456"
                        }
                        """.formatted(emailUnico("semadmin")))
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'admin' }.userMessage", equalTo("Admin é obrigatório"));
    }

    @Test
    @DisplayName("Listar usuários exige ADMIN")
    void quandoListarUsuariosComoAdmin_deveRetornar200() {
        givenAdmin()
                .when()
                .get("/api/usuarios")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", not(empty()));
    }
}
