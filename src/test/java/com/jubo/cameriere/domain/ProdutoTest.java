package com.jubo.cameriere.domain;

import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import org.junit.jupiter.api.Test;
import jakarta.validation.*;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProdutoTest {

    private final Validator validator;

    public ProdutoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UnidadeMedida criarUnidadeMedidaValida() {
        UnidadeMedida unidade = new UnidadeMedida();
        unidade.setSigla("UN");
        unidade.setDescricao("Unidade");
        return unidade;
    }

    @Test
    public void quandoNomeENulo_deveRetornarErro() {
        Produto produto = new Produto();
        produto.setPrecoCusto(100.0);
        produto.setUnidadeMedida(criarUnidadeMedidaValida());

        Set<String> mensagensErro = validator.validate(produto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Nome é obrigatório"));
    }

    @Test
    public void quandoPrecoCustoNegativo_deveRetornarErro() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setPrecoCusto(-50.0);
        produto.setUnidadeMedida(criarUnidadeMedidaValida());

        Set<String> mensagensErro = validator.validate(produto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Preço de custo deve ser maior que zero"));
    }

    @Test
    public void quandoPrecoCustoNulo_deveRetornarErro() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setUnidadeMedida(criarUnidadeMedidaValida());

        Set<String> mensagensErro = validator.validate(produto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Preço de custo é obrigatório"));
    }

    @Test
    public void quandoUnidadeMedidaNula_deveRetornarErro() {
        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setPrecoCusto(100.0);
        // unidadeMedida não é setada (null)

        Set<String> mensagensErro = validator.validate(produto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Unidade de medida é obrigatória"));
    }

    @Test
    public void quandoDadosValidos_devePassarNaValidacao() {
        Produto produto = new Produto();
        produto.setNome("Produto Válido");
        produto.setPrecoCusto(150.0);
        produto.setUnidadeMedida(criarUnidadeMedidaValida());
        produto.setAtivo(true);

        Set<ConstraintViolation<Produto>> violacoes = validator.validate(produto);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    public void quandoUnidadeMedidaComSiglaInvalida_deveRetornarErroNaUnidade() {
        UnidadeMedida unidadeInvalida = new UnidadeMedida();
        unidadeInvalida.setSigla(""); // Sigla vazia - inválida
        unidadeInvalida.setDescricao("Descrição válida");

        Produto produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setPrecoCusto(100.0);
        produto.setUnidadeMedida(unidadeInvalida);

        Set<ConstraintViolation<Produto>> violacoes = validator.validate(produto);

        // A validação deve falhar devido à unidade de medida inválida
        assertFalse(violacoes.isEmpty());
    }
}