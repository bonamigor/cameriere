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
public class ResumoEstanteResponseDTO {

    private Long id;
    private String periodo;
    private String clienteNome;
    private String clienteCnpj;
    private Boolean ativa;

    // Totais
    private Integer totalProdutos;
    private Integer totalEstoque;
    private BigDecimal valorTotalEstoque;

    // Médias
    private BigDecimal ticketMedioProduto;
    private BigDecimal quantidadeMediaPorProduto;

    // Análises
    private List<ProdutoEstoqueDTO> produtosComEstoqueBaixo;
    private List<ProdutoEstoqueDTO> produtosMaisEstocados;
    private List<ProdutoEstoqueDTO> produtosMaiorValor;

    // Distribuição
    private DistribuicaoDTO distribuicao;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoEstoqueDTO {
        private Long produtoId;
        private String produtoNome;
        private String unidadeMedida;
        private Integer quantidadeEstoque;
        private BigDecimal precoVenda;
        private BigDecimal valorTotal;
        private BigDecimal participacaoPercentual;
        private String localizacao;

        public void calcularParticipacao(BigDecimal valorTotalEstoque) {
            if (valorTotalEstoque != null && valorTotalEstoque.compareTo(BigDecimal.ZERO) != 0) {
                this.participacaoPercentual = this.valorTotal
                        .divide(valorTotalEstoque, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistribuicaoDTO {
        private Integer produtosAltoValor; // > R$ 1000
        private Integer produtosMedioValor; // R$ 100 - R$ 1000
        private Integer produtosBaixoValor; // < R$ 100

        private Integer produtosAltoEstoque; // > 1000 unidades
        private Integer produtosMedioEstoque; // 100 - 1000 unidades
        private Integer produtosBaixoEstoque; // < 100 unidades
    }

    public void calcularMetricas() {
        // Calcula ticket médio por produto
        if (totalProdutos != null && totalProdutos > 0 && valorTotalEstoque != null) {
            this.ticketMedioProduto = valorTotalEstoque
                    .divide(BigDecimal.valueOf(totalProdutos), 2, java.math.RoundingMode.HALF_UP);
        }

        // Calcula quantidade média por produto
        if (totalProdutos != null && totalProdutos > 0 && totalEstoque != null) {
            this.quantidadeMediaPorProduto = BigDecimal.valueOf(totalEstoque)
                    .divide(BigDecimal.valueOf(totalProdutos), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}