package com.jubo.cameriere.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@DisplayName("API Unidades de Medida")
class UnidadeMedidaTest extends ApiAuthSupport {

    @Test
    @DisplayName("Criar unidade válida retorna 201")
    void quandoCriarUnidadeMedidaComDadosValidos_deveRetornar201() {
        String sigla = siglaUnica();

        givenAdmin()
                .body("""
                        {
                            "sigla": "%s",
                            "descricao": "Quilograma"
                        }
                        """.formatted(sigla))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("sigla", equalTo(sigla))
                .body("descricao", equalTo("Quilograma"))
                .header("Location", containsString("/api/unidades-medida/"));
    }

    @Test
    @DisplayName("Criar unidade sem sigla retorna 400")
    void quandoCriarUnidadeMedidaSemSigla_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "descricao": "Descrição sem sigla"
                        }
                        """)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("title", equalTo("Dados inválidos"))
                .body("objects.find { it.name == 'sigla' }.userMessage", equalTo("Sigla é obrigatória"));
    }

    @Test
    @DisplayName("Criar unidade com sigla longa retorna 400")
    void quandoCriarUnidadeMedidaComSiglaLonga_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "sigla": "KILOGRAMS",
                            "descricao": "Descrição teste"
                        }
                        """)
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'sigla' }.userMessage",
                        equalTo("Sigla não pode exceder 4 caracteres"));
    }

    @Test
    @DisplayName("Criar unidade sem descrição retorna 400")
    void quandoCriarUnidadeMedidaSemDescricao_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "sigla": "%s"
                        }
                        """.formatted(siglaUnica()))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'descricao' }.userMessage",
                        equalTo("Descrição é obrigatória"));
    }

    @Test
    @DisplayName("Criar unidade com descrição longa retorna 400")
    void quandoCriarUnidadeMedidaComDescricaoLonga_deveRetornar400() {
        givenAdmin()
                .body("""
                        {
                            "sigla": "%s",
                            "descricao": "Esta descrição é muito longa e deve exceder o limite de cinquenta caracteres permitidos"
                        }
                        """.formatted(siglaUnica()))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("objects.find { it.name == 'descricao' }.userMessage",
                        equalTo("Descrição não pode exceder 50 caracteres"));
    }

    @Test
    @DisplayName("Criar unidade com sigla duplicada retorna 409")
    void quandoCriarUnidadeComSiglaDuplicada_deveRetornar409() {
        String sigla = siglaUnica();

        givenAdmin()
                .body("""
                        {
                            "sigla": "%s",
                            "descricao": "Primeira"
                        }
                        """.formatted(sigla))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        givenAdmin()
                .body("""
                        {
                            "sigla": "%s",
                            "descricao": "Duplicada"
                        }
                        """.formatted(sigla))
                .when()
                .post("/api/unidades-medida")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }
}
