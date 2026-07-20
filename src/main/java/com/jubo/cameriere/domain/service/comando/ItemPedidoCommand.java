package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record ItemPedidoCommand(
        Long produtoId,
        Long estanteId,
        BigDecimal quantidade,
        String observacao
) {
}
