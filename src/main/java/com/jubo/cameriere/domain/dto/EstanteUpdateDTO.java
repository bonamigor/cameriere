package com.jubo.cameriere.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstanteUpdateDTO {

    @Pattern(regexp = "\\d{4}/\\d{2}", message = "Período deve estar no formato YYYY/MM (ex: 2024/01)")
    private String periodo;

    private String observacao;

    private Boolean ativa;

    private Integer empresa;
}