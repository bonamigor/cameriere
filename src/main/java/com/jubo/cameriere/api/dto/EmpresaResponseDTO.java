package com.jubo.cameriere.api.dto;

import java.time.LocalDateTime;

public record EmpresaResponseDTO(
        Long id,
        String nome,
        String cnpj,
        EnderecoDTO endereco,
        String email,
        String telefone,
        Boolean ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
