package com.jubo.cameriere.api.dto;

import com.jubo.cameriere.domain.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        StatusPedido status,
        LocalDateTime dataCriacao,
        LocalDateTime dataConfirmacao,
        LocalDate dataPrevisaoEntrega,
        LocalDateTime dataCancelamento,
        LocalDateTime dataEntrega,
        BigDecimal valorTotal,
        Boolean entregue,
        String observacao,
        String obsCancelamento,
        Boolean finalizado,
        ClienteResumoDTO cliente,
        EmpresaResumoDTO empresaNota,
        List<ItemPedidoResponseDTO> itens
) {

    public static record ClienteResumoDTO(
            Long id,
            String nome,
            String cnpj,
            EnderecoDTO endereco,
            String telefone,
            Long empresaId,
            String empresaNome
    ) {}

    public static record EmpresaResumoDTO(
            Long id,
            String nome,
            String cnpj,
            EnderecoDTO endereco,
            String telefone
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
