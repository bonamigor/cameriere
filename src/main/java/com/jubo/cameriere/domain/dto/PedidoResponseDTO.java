package com.jubo.cameriere.domain.dto;

import com.jubo.cameriere.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        StatusPedido status,
        LocalDateTime dataCriacao,
        LocalDateTime dataConfirmacao,
        LocalDateTime dataCancelamento,
        LocalDateTime dataEntrega,
        BigDecimal valorTotal,
        Boolean entregue,
        String observacao,
        String obsCancelamento,
        Boolean finalizado,
        ClienteResumoDTO cliente,
        List<ItemPedidoResponseDTO> itens
) {

    public static record ClienteResumoDTO(
            Long id,
            String nome,
            String cnpj
    ) {}

    public static record ItemPedidoResponseDTO(
            Long id,
            BigDecimal quantidade,
            BigDecimal precoTotal,
            BigDecimal precoUnitario,
            String observacao,
            ProdutoResumoDTO produto
    ) {
        public static record ProdutoResumoDTO(
                Long id,
                String nome,
                String unidadeMedida
        ) {}
    }
}
