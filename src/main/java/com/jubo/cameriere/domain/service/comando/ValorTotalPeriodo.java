package com.jubo.cameriere.domain.service.comando;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ValorTotalPeriodo(
        LocalDateTime inicio,
        LocalDateTime fim,
        BigDecimal valorTotal
) {
}
