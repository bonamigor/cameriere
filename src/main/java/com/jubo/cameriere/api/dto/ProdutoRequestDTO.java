package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
        String nome,

        @NotNull(message = "Preço de custo é obrigatório")
        @Positive(message = "Preço de custo deve ser maior que zero")
        Double precoCusto,

        @NotBlank(message = "Sigla da unidade de medida é obrigatória")
        @Size(max = 4, message = "Sigla não pode exceder 4 caracteres")
        String unidadeMedidaSigla,

        Boolean ativo
) {
}
