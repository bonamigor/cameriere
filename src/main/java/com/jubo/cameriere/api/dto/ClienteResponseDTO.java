package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cnpj,
        EnderecoDTO endereco,
        String email,
        String telefone,
        Long empresaId,
        String empresaNome,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
