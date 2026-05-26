package com.jubo.cameriere.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsuarioTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void quandoCriarUsuarioComDadosValidos_deveRetornar201() {
        String corpoRequisicao = """
        {
            "nome": "João Silva",
            "email": "joao.silva@empresa.com",
            "senha": "123456",
            "admin": false
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("João Silva"))
                .body("email", equalTo("joao.silva@empresa.com"))
                .body("admin", equalTo(false));
    }

    @Test
    public void quandoCriarUsuarioComEmailInvalido_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "João Silva",
            "email": "email-invalido",
            "senha": "123456",
            "admin": false
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'email' }.userMessage", equalTo("Email deve ser válido"));
    }

    @Test
    public void quandoCriarUsuarioComSenhaCurta_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "João Silva",
            "email": "joao@empresa.com",
            "senha": "123",
            "admin": false
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'senha' }.userMessage", equalTo("Senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    public void quandoCriarUsuarioSemNome_deveRetornar400() {
        String corpoRequisicao = """
        {
            "email": "joao@empresa.com",
            "senha": "123456",
            "admin": false
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }

    @Test
    public void quandoCriarUsuarioSemAdmin_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "João Silva",
            "email": "joao@empresa.com",
            "senha": "123456"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/usuarios")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'admin' }.userMessage", equalTo("Admin é obrigatório"));
    }
}