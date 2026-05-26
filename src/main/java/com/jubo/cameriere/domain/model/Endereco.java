package com.jubo.cameriere.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Size(max = 255, message = "Logradouro não pode exceder 255 caracteres")
    private String logradouro;

    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    @Size(max = 9)
    private String cep;

    @Size(max = 255, message = "Cidade não pode exceder 255 caracteres")
    private String cidade;

    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
    private String estado;
}