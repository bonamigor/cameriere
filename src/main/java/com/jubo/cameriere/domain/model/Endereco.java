package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Size(max = 255, message = "Logradouro não pode exceder 255 caracteres")
    @Column(name = "endereco")
    private String logradouro;

    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    @Column(length = 9)
    private String cep;

    @Size(max = 255, message = "Cidade não pode exceder 255 caracteres")
    @Column
    private String cidade;

    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
    @Column(length = 2)
    private String estado;
}
