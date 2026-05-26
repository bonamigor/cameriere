package com.jubo.cameriere.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClienteTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void quandoCriarClienteComDadosValidos_deveRetornar201() {
        String corpoRequisicao = """
        {
            "nome": "Empresa Teste LTDA",
            "cnpj": "12.345.678/0001-90",
            "email": "contato@empresa.com",
            "telefone": "(11) 99999-9999",
            "endereco": {
                "logradouro": "Rua Exemplo, 123",
                "cep": "12345-678",
                "cidade": "São Paulo",
                "estado": "SP"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("nome", equalTo("Empresa Teste LTDA"));
    }

    @Test
    public void quandoCriarClienteComCNPJInvalido_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "Empresa Inválida",
            "cnpj": "12345678901234",
            "endereco": {
                "cep": "12345-678",
                "estado": "SP"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'cnpj' }.userMessage", equalTo("CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX"));

    }

    @Test
    public void quandoCriarClienteSemNome_deveRetornar400() {
        String corpoRequisicao = """
        {
            "cnpj": "12.345.678/0001-90",
            "endereco": {
                "estado": "SP"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'nome' }.userMessage", equalTo("Nome é obrigatório"));
    }

    @Test
    public void quandoCriarClienteComCEPInvalido_deveRetornar400() {
        String corpoRequisicao = """
        {
            "nome": "Empresa Teste",
            "cnpj": "12.345.678/0001-90",
            "endereco": {
                "cep": "12345678",
                "estado": "SP"
            }
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Dados inválidos"))
                .body("objects.size()", greaterThan(0))
                .body("objects.find { it.name == 'endereco.cep' }.userMessage", equalTo("CEP deve estar no formato XXXXX-XXX"));
    }

    @Test
    public void quandoCriarCliente_deveIncluirDatasDeCriacaoEAtualizacao() {
        String corpoRequisicao = """
    {
        "nome": "Empresa Teste LTDA",
        "cnpj": "12.345.679/0001-90",
        "email": "contato@empresa.com",
        "telefone": "(11) 99999-9999",
        "endereco": {
            "logradouro": "Rua Exemplo, 123",
            "cep": "12345-678",
            "cidade": "São Paulo",
            "estado": "SP"
        }
    }
    """;

        given()
                .contentType(ContentType.JSON)
                .body(corpoRequisicao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("dataCriacao", notNullValue())
                .body("dataAtualizacao", notNullValue());
    }

    @Test
    public void quandoAtualizarCliente_deveAtualizarDataAtualizacao() {
        // Primeiro cria um cliente
        String corpoCriacao = """
    {
        "nome": "Empresa Para Atualizar",
        "cnpj": "12.345.678/0001-91",
        "email": "atualizar@empresa.com",
        "endereco": {
            "logradouro": "Rua Teste, 123",
            "cep": "12345-678",
            "cidade": "São Paulo",
            "estado": "SP"
        }
    }
    """;


        Response responseCriacao = given()
                .contentType(ContentType.JSON)
                .body(corpoCriacao)
                .when()
                .post("/api/clientes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response();

        Long id = responseCriacao.jsonPath().getLong("id");
        LocalDateTime dataCriacaoOriginal = LocalDateTime.parse(responseCriacao.jsonPath().getString("dataCriacao").split("\\.")[0]);
        String dataAtualizacaoOriginal = responseCriacao.jsonPath().getString("dataAtualizacao");

        // Atualiza o cliente
        String corpoAtualizacao = """
    {
        "id": %d,
        "nome": "Empresa Atualizada",
        "cnpj": "12.345.678/0001-91",
        "email": "atualizado@empresa.com",
        "endereco": {
            "logradouro": "Rua Atualizada, 456",
            "cep": "12345-678",
            "cidade": "São Paulo",
            "estado": "SP"
        }
    }
    """.formatted(id);

        Response responseAtualizacao = given()
                .contentType(ContentType.JSON)
                .body(corpoAtualizacao)
                .when()
                .put("/api/clientes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response();

        Long idResposta = responseAtualizacao.jsonPath().getLong("id");
        LocalDateTime dataCriacaoResposta = LocalDateTime.parse(responseAtualizacao.jsonPath().getString("dataCriacao").split("\\.")[0]);
        String dataAtualizacaoResposta = responseAtualizacao.jsonPath().getString("dataAtualizacao");
        String nomeResposta = responseAtualizacao.jsonPath().getString("nome");

        // Validações
        assertEquals(id.intValue(), idResposta.intValue());
        assertEquals(dataCriacaoOriginal, dataCriacaoResposta); // Data criação deve ser igual
        assertNotNull(dataAtualizacaoResposta); // Data atualização não pode ser nula
        assertNotEquals(dataAtualizacaoOriginal, dataAtualizacaoResposta); // Deve ter mudado
        assertEquals("Empresa Atualizada", nomeResposta);
    }
}
