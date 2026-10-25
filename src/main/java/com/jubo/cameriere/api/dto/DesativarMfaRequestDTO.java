package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesativarMfaRequestDTO(
        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @NotBlank(message = "Código é obrigatório")
        @Size(max = 20, message = "Código inválido")
        String codigo
) {
}
