package com.jubo.cameriere.domain.service.comando;

import java.util.List;

public record PedidoCreateCommand(
        Long clienteId,
        String observacao,
        List<ItemPedidoCommand> itens
) {
}
