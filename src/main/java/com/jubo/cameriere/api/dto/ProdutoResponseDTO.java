package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        Double precoCusto,
        String unidadeMedidaSigla,
        String unidadeMedidaDescricao,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
