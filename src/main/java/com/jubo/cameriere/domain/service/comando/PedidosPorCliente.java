package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;

public record PedidosPorCliente(
        Long clienteId,
        String clienteNome,
        Long totalPedidos,
        BigDecimal valorTotal
) {
}
