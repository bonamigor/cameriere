package com.jubo.cameriere.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ValorTotalPeriodoDTO(
        LocalDateTime inicio,
        LocalDateTime fim,
        BigDecimal valorTotal
) {
}
