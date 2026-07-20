package com.jubo.cameriere.domain.service.comando;

public record EstanteUpdateCommand(
        String periodo,
        String observacao,
        Boolean ativa,
        Integer empresa
) {
}
