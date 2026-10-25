package com.jubo.cameriere.domain;

import com.jubo.cameriere.domain.exception.EstoqueInsuficienteException;
import com.jubo.cameriere.domain.model.EstanteProduto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Saldo do edital com quantidades fracionadas")
class EstanteProdutoSaldoTest {

    private EstanteProduto comSaldo(String saldo) {
        EstanteProduto ep = new EstanteProduto();
        ep.setAtivo(true);
        ep.setQuantidade(new BigDecimal(saldo));
        return ep;
    }

    @Test
    @DisplayName("Baixa e reposição usam o valor exato, sem arredondar")
    void quandoBaixarEReporFracao_deveManterValorExato() {
        EstanteProduto ep = comSaldo("100");

        ep.baixarEstoque(new BigDecimal("35.5"));
        assertEquals(0, ep.getQuantidade().compareTo(new BigDecimal("64.5")));

        ep.reporEstoque(new BigDecimal("35.5"));
        assertEquals(0, ep.getQuantidade().compareTo(new BigDecimal("100")));
    }

    @Test
    @DisplayName("Não permite baixar mais do que o saldo, nem por uma fração")
    void quandoPedidoExcedeSaldoPorFracao_deveRecusar() {
        EstanteProduto ep = comSaldo("10.5");

        assertTrue(ep.possuiEstoqueSuficiente(new BigDecimal("10.5")));
        assertFalse(ep.possuiEstoqueSuficiente(new BigDecimal("10.501")));
        assertThrows(EstoqueInsuficienteException.class, () -> ep.baixarEstoque(new BigDecimal("10.501")));
        assertEquals(0, ep.getQuantidade().compareTo(new BigDecimal("10.5")));
    }

    @Test
    @DisplayName("Saldo fracionado acima de zero continua disponível")
    void quandoSaldoMenorQueUm_deveContinuarDisponivel() {
        assertTrue(comSaldo("0.25").isDisponivel());
        assertFalse(comSaldo("0").isDisponivel());
    }
}
