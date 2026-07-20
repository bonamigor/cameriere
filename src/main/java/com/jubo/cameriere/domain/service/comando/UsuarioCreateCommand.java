package com.jubo.cameriere.domain.service.comando;

public record UsuarioCreateCommand(
        String nome,
        String email,
        String senha,
        Boolean admin,
        Long clienteId
) {
}
