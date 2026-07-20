package com.jubo.cameriere.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
        String nome,

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
                message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
        String cnpj,

        @Valid
        EnderecoDTO endereco,

        @Email(message = "Email deve ser válido")
        @Size(max = 255, message = "Email não pode exceder 255 caracteres")
        String email,

        @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
        String telefone,

        Long empresaId,

        Boolean ativo
) {
}
