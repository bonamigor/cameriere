package com.jubo.cameriere.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@DisplayName("API Estante x Produto")
class EstanteProdutoTest extends ApiAuthSupport {

    private Long estanteId;
    private Long produtoId;

    @BeforeEach
    void prepararDados() {
        Number clienteId = givenAdmin()
                .body("""
                        {
                          "nome": "Cliente Estante Teste",
                          "cnpj": "%s",
                          "email": "%s",
                          "endereco": { "cep": "01310-100", "cidade": "São Paulo", "estado": "SP" }
                        }
                        """.formatted(cnpjUnico(), emailUnico("estante")))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        String periodo = "20%02d/%02d".formatted(
                (Math.abs(clienteId.intValue()) % 80) + 20,
                (Math.abs(clienteId.intValue()) % 12) + 1);

        Number estanteIdNum = givenAdmin()
                .body("""
                        {
                          "periodo": "%s",
                          "observacao": "Estante teste",
                          "clienteId": %d,
                          "ativa": true,
                          "empresa": 1
                        }
                        """.formatted(periodo, clienteId.longValue()))
                .when()
                .post("/api/estantes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");
        estanteId = estanteIdNum.longValue();

        String sigla = siglaUnica();
        givenAdmin()
                .body("""
                        { "sigla": "%s", "descricao": "Unidade EP" }
                        """.formatted(sigla))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        Number produtoIdNum = givenAdmin()
                .body("""
                        {
                          "nome": "Produto EP %s",
                          "precoCusto": 10.0,
                          "unidadeMedidaSigla": "%s",
                          "ativo": true
                        }
                        """.formatted(sigla, sigla))
                .when()
                .post("/api/produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");
        produtoId = produtoIdNum.longValue();
    }

    @Test
    @DisplayName("Associar produto com preço >= custo retorna 201")
    void quandoAssociarComPrecoValido_deveRetornar201() {
        givenAdmin()
                .body("""
                        {
                          "estanteId": %d,
                          "produtoId": %d,
                          "precoVenda": 15.00,
                          "quantidadeEstoque": 50
                        }
                        """.formatted(estanteId, produtoId))
                .when()
                .post("/api/estantes-produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("precoVenda", equalTo(15.0f))
                .body("quantidade", equalTo(50));
    }

    @Test
    @DisplayName("Associar produto com preço < custo retorna 400")
    void quandoAssociarComPrecoMenorQueCusto_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                          "estanteId": %d,
                          "produtoId": %d,
                          "precoVenda": 5.00,
                          "quantidadeEstoque": 10
                        }
                        """.formatted(estanteId, produtoId))
                .when()
                .post("/api/estantes-produtos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("title", equalTo("Violação de regra de negócio"));
    }

    @Test
    @DisplayName("Atualizar associação com preço < custo retorna 400")
    void quandoAtualizarComPrecoMenorQueCusto_deveRetornar400() {
        Number associacaoId = givenAdmin()
                .body("""
                        {
                          "estanteId": %d,
                          "produtoId": %d,
                          "precoVenda": 20.00,
                          "quantidadeEstoque": 10
                        }
                        """.formatted(estanteId, produtoId))
                .when()
                .post("/api/estantes-produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        givenAdmin()
                .body("""
                        {
                          "precoVenda": 1.00
                        }
                        """)
                .when()
                .put("/api/estantes-produtos/{id}", associacaoId.longValue())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("detail", containsString("não pode ser menor que o custo"));
    }
}
