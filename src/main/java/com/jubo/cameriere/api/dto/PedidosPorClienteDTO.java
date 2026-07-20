package com.jubo.cameriere.api.dto;

import java.math.BigDecimal;

public record PedidosPorClienteDTO(
        Long clienteId,
        String clienteNome,
        Long totalPedidos,
        BigDecimal valorTotal
) {
}
