package com.jubo.cameriere.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@DisplayName("API Produtos")
class ProdutoTest extends ApiAuthSupport {

    private String unidadeSigla;

    @BeforeEach
    void prepararUnidadeMedida() {
        unidadeSigla = siglaUnica();
        givenAdmin()
                .body("""
                        {
                            "sigla": "%s",
                            "descricao": "Unidade teste"
                        }
                        """.formatted(unidadeSigla))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Criar produto válido retorna 201")
    void quandoCriarProdutoComDadosValidos_deveRetornar201() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Notebook Dell Inspiron",
                            "precoCusto": 2500.50,
                            "unidadeMedidaSigla": "%s",
                            "ativo": true
                        }
                        """.formatted(unidadeSigla))
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("Notebook Dell Inspiron"))
                .body("precoCusto", equalTo(2500.50f))
                .body("unidadeMedidaSigla", equalTo(unidadeSigla))
                .body("ativo", equalTo(true))
                .header("Location", containsString("/api/produtos/"));
    }

    @Test
    @DisplayName("Criar produto sem unidade de medida retorna 400")
    void quandoCriarProdutoSemUnidadeMedida_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Produto Teste",
                            "precoCusto": 100.0
                        }
                        """)
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("title", equalTo("Dados inválidos"))
                .body("objects.find { it.name == 'unidadeMedidaSigla' }.userMessage",
                        equalTo("Sigla da unidade de medida é obrigatória"));
    }

    @Test
    @DisplayName("Criar produto com preço negativo retorna 400")
    void quandoCriarProdutoComPrecoCustoNegativo_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Produto Inválido",
                            "precoCusto": -50.0,
                            "unidadeMedidaSigla": "%s"
                        }
                        """.formatted(unidadeSigla))
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'precoCusto' }.userMessage",
                        equalTo("Preço de custo deve ser maior que zero"));
    }

    @Test
    @DisplayName("Criar produto sem nome retorna 400")
    void quandoCriarProdutoSemNome_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "precoCusto": 100.0,
                            "unidadeMedidaSigla": "%s"
                        }
                        """.formatted(unidadeSigla))
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }

    @Test
    @DisplayName("Listar produtos autenticado retorna 200")
    void quandoListarProdutos_deveRetornar200() {
        givenAdmin()
                .when()
                .get("/api/produtos")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
