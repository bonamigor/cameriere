package com.jubo.cameriere.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReajusteResponseDTO {

    private Long estanteId;
    private String estantePeriodo;
    private BigDecimal percentual;
    private Integer quantidadeProdutosAfetados;
    private String motivo;
    private LocalDateTime dataReajuste;
    private Long usuarioResponsavelId;
    private String usuarioNome;

    // Faixas de preço antes e depois
    private BigDecimal valorMinimoAntes;
    private BigDecimal valorMaximoAntes;
    private BigDecimal valorMinimoDepois;
    private BigDecimal valorMaximoDepois;

    // Resumo
    private BigDecimal valorTotalEstoqueAntes;
    private BigDecimal valorTotalEstoqueDepois;
    private BigDecimal impactoFinanceiro;

    public ReajusteResponseDTO(Long estanteId, BigDecimal percentual, Integer quantidadeProdutosAfetados, String motivo) {
        this.estanteId = estanteId;
        this.percentual = percentual;
        this.quantidadeProdutosAfetados = quantidadeProdutosAfetados;
        this.motivo = motivo;
        this.dataReajuste = LocalDateTime.now();
    }

    public void calcularImpactoFinanceiro() {
        if (valorTotalEstoqueAntes != null && valorTotalEstoqueDepois != null) {
            this.impactoFinanceiro = valorTotalEstoqueDepois.subtract(valorTotalEstoqueAntes);
        }
    }

    public String getDescricao() {
        String tipo = percentual.compareTo(BigDecimal.ZERO) > 0 ? "aumento" : "redução";
        return String.format("%s de %.2f%% nos preços da estante %d",
                tipo, percentual.abs(), estanteId);
    }
}