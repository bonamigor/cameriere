package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record PedidoFiltroResumo(
        long quantidadePedidos,
        BigDecimal valorTotal
) {
}
