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
public class ItemPedidoResponseDTO {

    private Long id;
    private BigDecimal quantidade;
    private BigDecimal precoTotal;
    private BigDecimal precoUnitario;
    private String observacao;

    // Informações do Produto
    private ProdutoResumoDTO produto;

    // Informações da Estante
    private EstanteResumoDTO estante;

    // Informações do Pedido (resumidas)
    private PedidoResumoDTO pedido;

    // Metadados
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // Informações de Estoque no momento do pedido
    private EstoqueInfoDTO estoqueInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoResumoDTO {
        private Long id;
        private String nome;
        private String codigo;
        private String unidadeMedida;
        private String unidadeMedidaSigla;
        private BigDecimal precoCusto;
        private BigDecimal margemLucro;
        private Boolean ativo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstanteResumoDTO {
        private Long id;
        private String periodo;
        private String observacao;
        private Long clienteId;
        private String clienteNome;
        private Boolean ativa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoResumoDTO {
        private Long id;
        private String status;
        private LocalDateTime dataCriacao;
        private BigDecimal valorTotal;
        private String clienteNome;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstoqueInfoDTO {
        private Integer quantidadeDisponivel;
        private Integer quantidadeReservada;
        private Boolean disponivelParaVenda;
        private String localizacaoPrateleira;
    }

    // Métodos utilitários
    public void calcularPrecoUnitario() {
        if (quantidade != null && quantidade.compareTo(BigDecimal.ZERO) != 0 && precoTotal != null) {
            this.precoUnitario = precoTotal.divide(quantidade, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.precoUnitario = BigDecimal.ZERO;
        }
    }

    public void calcularMargemLucro(BigDecimal precoCusto) {
        if (produto != null && precoUnitario != null && precoCusto != null) {
            BigDecimal lucro = precoUnitario.subtract(precoCusto);
            BigDecimal margem = lucro.divide(precoUnitario, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            produto.setMargemLucro(margem);
        }
    }

    public BigDecimal getSubtotal() {
        return precoTotal != null ? precoTotal : BigDecimal.ZERO;
    }

    public String getDescricaoCompleta() {
        StringBuilder desc = new StringBuilder();
        if (produto != null) {
            desc.append(produto.getNome());
        }
        if (quantidade != null) {
            desc.append(" - ").append(quantidade);
            if (produto != null && produto.getUnidadeMedidaSigla() != null) {
                desc.append(" ").append(produto.getUnidadeMedidaSigla());
            }
        }
        if (precoTotal != null) {
            desc.append(" - R$ ").append(precoTotal);
        }
        return desc.toString();
    }

    public boolean isPrecoAlterado(BigDecimal precoOriginal) {
        return precoUnitario != null && precoOriginal != null &&
                precoUnitario.compareTo(precoOriginal) != 0;
    }

    public BigDecimal getDiferencaPreco(BigDecimal precoOriginal) {
        if (precoUnitario != null && precoOriginal != null) {
            return precoUnitario.subtract(precoOriginal);
        }
        return BigDecimal.ZERO;
    }
}