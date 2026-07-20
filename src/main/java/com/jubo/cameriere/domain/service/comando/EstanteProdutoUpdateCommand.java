package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record EstanteProdutoUpdateCommand(
        BigDecimal precoVenda,
        Integer quantidadeEstoque,
        Boolean ativo
) {
}
