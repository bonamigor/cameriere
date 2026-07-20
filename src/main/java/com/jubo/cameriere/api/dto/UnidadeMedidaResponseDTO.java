package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record UnidadeMedidaResponseDTO(
        Long id,
        String sigla,
        String descricao,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
