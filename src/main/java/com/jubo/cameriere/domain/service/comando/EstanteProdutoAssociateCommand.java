package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record EstanteProdutoAssociateCommand(
        Long estanteId,
        Long produtoId,
        BigDecimal precoVenda,
        BigDecimal quantidadeEstoque
) {
}
