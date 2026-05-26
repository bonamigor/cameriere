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
public class ProdutoTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void quandoCriarProdutoComDadosValidos_deveRetornar201() {
        String corpoRequisicao = """
        {
            "nome": "Notebook Dell Inspiron",
            "precoCusto": 2500.50,
            "unidadeMedida": {
                "id": 1,
                "sigla": "UN",
                "descricao": "Unidade"
            },
            "ativo": true
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("Notebook Dell Inspiron"))
                .body("precoCusto", equalTo(2500.50f))
                .body("ativo", equalTo(true));
    }

    @Test
    public void quandoCriarProdutoSemUnidadeMedida_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "Produto Teste",
            "precoCusto": 100.0
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'unidadeMedida' }.userMessage", equalTo("Unidade de medida é obrigatória"));
    }

    @Test
    public void quandoCriarProdutoComPrecoCustoNegativo_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "Produto Inválido",
            "precoCusto": -50.0,
            "unidadeMedida": {
                "id": 1,
                "sigla": "UN",
                "descricao": "Unidade"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'precoCusto' }.userMessage", equalTo("Preço de custo deve ser maior que zero"));
    }

    @Test
    public void quandoCriarProdutoSemNome_deveRetornar400() {
        String corpoRequisicao = """
        {
            "precoCusto": 100.0,
            "unidadeMedida": {
                "id": 1,
                "sigla": "UN",
                "descricao": "Unidade"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }
}