package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Boolean admin,
        Long clienteId,
        String clienteNome,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
