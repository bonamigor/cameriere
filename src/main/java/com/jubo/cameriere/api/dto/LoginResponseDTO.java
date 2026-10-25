package com.jubo.cameriere.api.dto;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        Long expiresInMs,
        Long usuarioId,
        String nome,
        String email,
        String role,
        Long clienteId,
        boolean doisFatoresAtivo
) {
}
