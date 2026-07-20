package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @Size(max = 255, message = "Logradouro não pode exceder 255 caracteres")
        String logradouro,

        @Size(max = 20, message = "Número não pode exceder 20 caracteres")
        String numero,

        @Size(max = 255, message = "Bairro não pode exceder 255 caracteres")
        String bairro,

        @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
        @Size(max = 9)
        String cep,

        @Size(max = 255, message = "Cidade não pode exceder 255 caracteres")
        String cidade,

        @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
        String estado
) {
}
