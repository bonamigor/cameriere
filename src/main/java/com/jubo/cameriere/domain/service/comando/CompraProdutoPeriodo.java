package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record CompraProdutoPeriodo(
        Long produtoId,
        String produtoNome,
        String unidadeMedida,
        BigDecimal quantidadeTotal,
        BigDecimal valorTotal,
        long quantidadePedidos
) {
}
