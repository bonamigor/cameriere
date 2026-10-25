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
    private String sigla;
    private Long clienteId;

    @BeforeEach
    void prepararDados() {
        Number clienteIdNum = givenAdmin()
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
        clienteId = clienteIdNum.longValue();

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
                        """.formatted(periodo, clienteId))
                .when()
                .post("/api/estantes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");
        estanteId = estanteIdNum.longValue();

        sigla = siglaUnica();
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
    @DisplayName("Itens disponíveis da estante trazem a unidade de medida do produto")
    void quandoListarDisponiveis_deveIncluirUnidadeDeMedida() {
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
                .statusCode(HttpStatus.CREATED.value());

        givenUser()
                .when()
                .get("/api/estantes-produtos/estante/{id}/disponiveis", estanteId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].produtoId", equalTo(produtoId.intValue()))
                .body("[0].unidadeMedidaSigla", equalTo(sigla))
                .body("[0].unidadeMedidaDescricao", equalTo("Unidade EP"))
                .body("[0].quantidade", equalTo(50));
    }

    @Test
    @DisplayName("Pedido fracionado confirmado baixa o saldo exato e o cancelamento o repõe")
    void quandoConfirmarECancelarPedidoFracionado_deveAjustarSaldoExato() {
        associar("100");

        Number pedidoId = givenAdmin()
                .body("""
                        {
                          "clienteId": %d,
                          "itens": [ { "produtoId": %d, "estanteId": %d, "quantidade": 35.5 } ]
                        }
                        """.formatted(clienteId, produtoId, estanteId))
                .when()
                .post("/api/pedidos")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .path("id");

        givenAdmin()
                .body("{ \"dataPrevisaoEntrega\": \"%s\" }".formatted(java.time.LocalDate.now().plusDays(2)))
                .when()
                .put("/api/pedidos/{id}/confirmar", pedidoId.longValue())
                .then()
                .statusCode(HttpStatus.OK.value());
        saldoDisponivel(64.5f);

        givenAdmin()
                .body("{ \"motivo\": \"Teste de reposição de saldo\" }")
                .when()
                .put("/api/pedidos/{id}/cancelar", pedidoId.longValue())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        saldoDisponivel(100);
    }

    @Test
    @DisplayName("Saldo e quantidade do item aceitam no máximo 3 casas decimais")
    void quandoQuantidadeTemMaisDeTresCasas_deveRetornar400() {
        associar("10.5");
        saldoDisponivel(10.5f);

        givenAdmin()
                .body("""
                        {
                          "clienteId": %d,
                          "itens": [ { "produtoId": %d, "estanteId": %d, "quantidade": 1.2345 } ]
                        }
                        """.formatted(clienteId, produtoId, estanteId))
                .when()
                .post("/api/pedidos")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private void associar(String saldo) {
        givenAdmin()
                .body("""
                        {
                          "estanteId": %d,
                          "produtoId": %d,
                          "precoVenda": 15.00,
                          "quantidadeEstoque": %s
                        }
                        """.formatted(estanteId, produtoId, saldo))
                .when()
                .post("/api/estantes-produtos")
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    private void saldoDisponivel(Object esperado) {
        givenUser()
                .when()
                .get("/api/estantes-produtos/estante/{id}/disponiveis", estanteId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("[0].quantidade", equalTo(esperado));
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
