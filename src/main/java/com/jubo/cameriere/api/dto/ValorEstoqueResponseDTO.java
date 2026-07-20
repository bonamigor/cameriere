package com.jubo.cameriere.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValorEstoqueResponseDTO {

    private Long estanteId;
    private String estantePeriodo;
    private String clienteNome;

    // Totais
    private BigDecimal valorTotalEstoque;
    private Integer quantidadeTotalProdutos;
    private Integer quantidadeTotalItens;

    // Médias
    private BigDecimal valorMedioPorProduto;
    private BigDecimal valorMedioPorItem;

    // Detalhamento
    private List<ProdutoEstoqueValorDTO> produtosPorValor;

    // Resumo por status
    private ResumoStatusDTO resumoStatus;

    public ValorEstoqueResponseDTO(Long estanteId, BigDecimal valorTotal) {
        this.estanteId = estanteId;
        this.valorTotalEstoque = valorTotal;
    }

    public void calcularMedias() {
        if (quantidadeTotalProdutos != null && quantidadeTotalProdutos > 0) {
            this.valorMedioPorProduto = valorTotalEstoque
                    .divide(BigDecimal.valueOf(quantidadeTotalProdutos), 2, java.math.RoundingMode.HALF_UP);
        }

        if (quantidadeTotalItens != null && quantidadeTotalItens > 0) {
            this.valorMedioPorItem = valorTotalEstoque
                    .divide(BigDecimal.valueOf(quantidadeTotalItens), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoEstoqueValorDTO {
        private Long produtoId;
        private String produtoNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal valorTotal;
        private BigDecimal participacaoPercentual;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumoStatusDTO {
        private Integer produtosAtivos;
        private Integer produtosInativos;
        private BigDecimal valorProdutosAtivos;
        private BigDecimal valorProdutosInativos;
    }
}