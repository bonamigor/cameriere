package com.jubo.cameriere.domain.service.comando;

public record EstanteCreateCommand(
        String periodo,
        String observacao,
        Long clienteId,
        Boolean ativa,
        Integer empresa
) {
}
