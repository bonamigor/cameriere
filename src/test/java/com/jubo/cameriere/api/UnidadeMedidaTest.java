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
public class UnidadeMedidaTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void quandoCriarUnidadeMedidaComDadosValidos_deveRetornar201() {
        String corpoRequisicao = """
        {
            "sigla": "KG",
            "descricao": "Quilograma"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("sigla", equalTo("KG"))
                .body("descricao", equalTo("Quilograma"));
    }

    @Test
    public void quandoCriarUnidadeMedidaSemSigla_deveRetornar400() {
        String corpoRequisicao = """
        {
            "descricao": "Descrição sem sigla"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'sigla' }.userMessage", equalTo("Sigla é obrigatória"));
    }

    @Test
    public void quandoCriarUnidadeMedidaComSiglaLonga_deveRetornar400() {
        String corpoRequisicao = """
        {
            "sigla": "KILOGRAMS",
            "descricao": "Descrição teste"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'sigla' }.userMessage", equalTo("Sigla não pode exceder 4 caracteres"));
    }

    @Test
    public void quandoCriarUnidadeMedidaSemDescricao_deveRetornar400() {
        String corpoRequisicao = """
        {
            "sigla": "UN"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'descricao' }.userMessage", equalTo("Descrição é obrigatória"));
    }

    @Test
    public void quandoCriarUnidadeMedidaComDescricaoLonga_deveRetornar400() {
        String corpoRequisicao = """
        {
            "sigla": "LT",
            "descricao": "Esta descrição é muito longa e deve exceder o limite de cinquenta caracteres permitidos"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'descricao' }.userMessage", equalTo("Descrição não pode exceder 50 caracteres"));
    }
}