package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginMfaRequestDTO(
        @NotBlank(message = "Token de verificação é obrigatório")
        String mfaToken,

        @NotBlank(message = "Código é obrigatório")
        @Size(max = 20, message = "Código inválido")
        String codigo
) {
}
