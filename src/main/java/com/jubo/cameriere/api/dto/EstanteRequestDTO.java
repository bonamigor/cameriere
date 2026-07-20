package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstanteRequestDTO {

    @NotBlank(message = "Período é obrigatório")
    @Pattern(regexp = "\\d{4}/\\d{2}", message = "Período deve estar no formato YYYY/MM")
    private String periodo;

    private String observacao;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    private Boolean ativa;

    private Integer empresa;
}