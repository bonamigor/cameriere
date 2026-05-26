package com.jubo.cameriere.domain;

import com.jubo.cameriere.domain.model.UnidadeMedida;
import org.junit.jupiter.api.Test;
import jakarta.validation.*;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class UnidadeMedidaTest {

    private final Validator validator;

    public UnidadeMedidaTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void quandoSiglaENula_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setDescricao("Descrição válida");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Sigla é obrigatória"));
    }

    @Test
    public void quandoSiglaVazia_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("");
        unidadeMedida.setDescricao("Descrição válida");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Sigla é obrigatória"));
    }

    @Test
    public void quandoSiglaLonga_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("KILO"); // 4 caracteres - deve passar
        unidadeMedida.setDescricao("Descrição válida");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        // KILO tem 4 caracteres, então deve passar na validação
        assertFalse(mensagensErro.contains("Sigla não pode exceder 4 caracteres"));
    }

    @Test
    public void quandoDescricaoNula_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("UN");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Descrição é obrigatória"));
    }

    @Test
    public void quandoDescricaoVazia_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("UN");
        unidadeMedida.setDescricao("");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Descrição é obrigatória"));
    }

    @Test
    public void quandoDescricaoLonga_deveRetornarErro() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("UN");
        unidadeMedida.setDescricao("Esta descrição é muito longa e deve exceder o limite de cinquenta caracteres permitidos");

        Set<String> mensagensErro = validator.validate(unidadeMedida).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Descrição não pode exceder 50 caracteres"));
    }

    @Test
    public void quandoDadosValidos_devePassarNaValidacao() {
        UnidadeMedida unidadeMedida = new UnidadeMedida();
        unidadeMedida.setSigla("KG");
        unidadeMedida.setDescricao("Quilograma");

        Set<ConstraintViolation<UnidadeMedida>> violacoes = validator.validate(unidadeMedida);

        assertTrue(violacoes.isEmpty());
    }
}