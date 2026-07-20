package com.jubo.cameriere.api.dto;

import java.math.BigDecimal;

public record PedidoFiltroResumoDTO(
        long quantidadePedidos,
        BigDecimal valorTotal
) {
}
