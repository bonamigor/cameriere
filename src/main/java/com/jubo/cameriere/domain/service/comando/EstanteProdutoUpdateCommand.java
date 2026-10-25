package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record EstanteProdutoUpdateCommand(
        BigDecimal precoVenda,
        BigDecimal quantidadeEstoque,
        Boolean ativo
) {
}
