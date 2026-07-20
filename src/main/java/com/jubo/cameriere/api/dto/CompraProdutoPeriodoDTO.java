package com.jubo.cameriere.api.dto;

import java.math.BigDecimal;

public record CompraProdutoPeriodoDTO(
        Long produtoId,
        String produtoNome,
        String unidadeMedida,
        BigDecimal quantidadeTotal,
        BigDecimal valorTotal,
        long quantidadePedidos
) {
}
