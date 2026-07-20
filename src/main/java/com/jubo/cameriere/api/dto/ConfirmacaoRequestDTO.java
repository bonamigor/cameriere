package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmacaoRequestDTO {

    @NotNull(message = "Data prevista de entrega é obrigatória")
    private LocalDate dataPrevisaoEntrega;
}
