package com.jubo.cameriere.api;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API Clientes")
class ClienteTest extends ApiAuthSupport {

    @Test
    @DisplayName("Criar cliente com dados válidos retorna 201")
    void quandoCriarClienteComDadosValidos_deveRetornar201() {
        String cnpj = cnpjUnico();

        givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Teste LTDA",
                            "cnpj": "%s",
                            "email": "%s",
                            "telefone": "(11) 99999-9999",
                            "endereco": {
                                "logradouro": "Rua Exemplo, 123",
                                "cep": "12345-678",
                                "cidade": "São Paulo",
                                "estado": "SP"
                            }
                        }
                        """.formatted(cnpj, emailUnico("contato")))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("Empresa Teste LTDA"))
                .body("cnpj", equalTo(cnpj))
                .header("Location", containsString("/api/clientes/"));
    }

    @Test
    @DisplayName("Criar cliente com CNPJ inválido retorna 400")
    void quandoCriarClienteComCNPJInvalido_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Inválida",
                            "cnpj": "12345678901234",
                            "endereco": {
                                "cep": "12345-678",
                                "estado": "SP"
                            }
                        }
                        """)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'cnpj' }.userMessage",
                        equalTo("CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX"));
    }

    @Test
    @DisplayName("Criar cliente sem nome retorna 400")
    void quandoCriarClienteSemNome_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "cnpj": "%s",
                            "endereco": { "estado": "SP" }
                        }
                        """.formatted(cnpjUnico()))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }

    @Test
    @DisplayName("Criar cliente com CEP inválido retorna 400")
    void quandoCriarClienteComCEPInvalido_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Teste",
                            "cnpj": "%s",
                            "endereco": {
                                "cep": "12345678",
                                "estado": "SP"
                            }
                        }
                        """.formatted(cnpjUnico()))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'endereco.cep' }.userMessage",
                        equalTo("CEP deve estar no formato XXXXX-XXX"));
    }

    @Test
    @DisplayName("Criar cliente inclui datas de auditoria")
    void quandoCriarCliente_deveIncluirDatasDeCriacaoEAtualizacao() {
        givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Auditoria",
                            "cnpj": "%s",
                            "email": "%s",
                            "endereco": {
                                "logradouro": "Rua Exemplo, 123",
                                "cep": "12345-678",
                                "cidade": "São Paulo",
                                "estado": "SP"
                            }
                        }
                        """.formatted(cnpjUnico(), emailUnico("audit")))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("dataCriacao", notNullValue())
                .body("dataAtualizacao", notNullValue());
    }

    @Test
    @DisplayName("Atualizar cliente altera dataAtualizacao")
    void quandoAtualizarCliente_deveAtualizarDataAtualizacao() throws InterruptedException {
        String cnpj = cnpjUnico();

        Response responseCriacao = givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Para Atualizar",
                            "cnpj": "%s",
                            "email": "%s",
                            "endereco": {
                                "logradouro": "Rua Teste, 123",
                                "cep": "12345-678",
                                "cidade": "São Paulo",
                                "estado": "SP"
                            }
                        }
                        """.formatted(cnpj, emailUnico("atualizar")))
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();

        Long id = responseCriacao.jsonPath().getLong("id");
        LocalDateTime dataCriacaoOriginal = LocalDateTime.parse(
                responseCriacao.jsonPath().getString("dataCriacao").split("\\.")[0]);
        String dataAtualizacaoOriginal = responseCriacao.jsonPath().getString("dataAtualizacao");

        Thread.sleep(1100);

        Response responseAtualizacao = givenAdmin()
                .body("""
                        {
                            "nome": "Empresa Atualizada",
                            "cnpj": "%s",
                            "email": "%s",
                            "endereco": {
                                "logradouro": "Rua Atualizada, 456",
                                "cep": "12345-678",
                                "cidade": "São Paulo",
                                "estado": "SP"
                            }
                        }
                        """.formatted(cnpj, emailUnico("atualizado")))
                .when()
                .put("/api/clientes/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();

        assertEquals(id, responseAtualizacao.jsonPath().getLong("id"));
        assertEquals(dataCriacaoOriginal, LocalDateTime.parse(
                responseAtualizacao.jsonPath().getString("dataCriacao").split("\\.")[0]));
        assertNotNull(responseAtualizacao.jsonPath().getString("dataAtualizacao"));
        assertNotEquals(dataAtualizacaoOriginal, responseAtualizacao.jsonPath().getString("dataAtualizacao"));
        assertEquals("Empresa Atualizada", responseAtualizacao.jsonPath().getString("nome"));
    }

    @Test
    @DisplayName("Buscar cliente inexistente retorna 404")
    void quandoBuscarClienteInexistente_deveRetornar404() {
        givenAdmin()
                .when()
                .get("/api/clientes/{id}", 999_999_999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
