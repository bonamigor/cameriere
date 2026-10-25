package com.jubo.cameriere.api.dto;

public record SessaoDTO(
        Long usuarioId,
        String nome,
        String email,
        String role,
        Long clienteId,
        boolean doisFatoresAtivo,
        long codigosRecuperacaoRestantes
) {
}
