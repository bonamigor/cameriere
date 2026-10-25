package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record AuditoriaResponseDTO(
        Long id,
        LocalDateTime dataHora,
        String acao,
        Long usuarioId,
        String usuarioEmail,
        String metodo,
        String recurso,
        Integer statusHttp,
        String ip,
        String detalhe
) {
}
