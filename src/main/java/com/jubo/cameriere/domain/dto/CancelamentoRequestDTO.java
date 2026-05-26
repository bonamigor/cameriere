package com.jubo.cameriere.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoRequestDTO {

    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    private String motivo;
}