package com.jubo.cameriere.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Long clienteId,

        String observacao,

        @NotEmpty(message = "Pedido deve ter pelo menos um item")
        @Valid
        List<ItemPedidoRequestDTO> itens
) {}
