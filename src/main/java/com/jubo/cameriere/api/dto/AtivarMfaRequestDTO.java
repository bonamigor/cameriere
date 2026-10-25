package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtivarMfaRequestDTO(
        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "\\d{6}", message = "Informe os 6 dígitos do aplicativo autenticador")
        String codigo
) {
}
