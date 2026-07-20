package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadeMedidaRequestDTO(
        @NotBlank(message = "Sigla é obrigatória")
        @Size(max = 4, message = "Sigla não pode exceder 4 caracteres")
        String sigla,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 50, message = "Descrição não pode exceder 50 caracteres")
        String descricao
) {
}
