package com.jubo.cameriere.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumoDTO {

    private Long id;
    private String nome;
    private String cnpj;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private Boolean ativo;

    // Informações adicionais
    private String endereco;
    private String cep;

    // Métricas do cliente
    private Integer totalPedidos;
    private BigDecimal valorTotalGasto;
    private BigDecimal ticketMedio;
    private Integer pedidosPendentes;
    private Integer pedidosConfirmados;
    private Integer pedidosEntregues;
    private Integer pedidosCancelados;

    // Últimos pedidos
    private LocalDateTime ultimoPedidoData;
    private BigDecimal ultimoPedidoValor;
    private Long ultimoPedidoId;

    // Análises
    private CategoriaCliente categoria;
    private Boolean isClienteRecente;
    private Boolean isClienteFrequente;
    private Boolean isClientePremium;

    // Estatísticas de tempo
    private Long diasDesdeUltimoPedido;
    private Double frequenciaMediaPedidosPorMes;

    public enum CategoriaCliente {
        BRONZE("Bronze", 0.0, 1000.0),
        PRATA("Prata", 1000.0, 5000.0),
        OURO("Ouro", 5000.0, 15000.0),
        DIAMANTE("Diamante", 15000.0, Double.MAX_VALUE);

        private String descricao;
        private Double limiteInferior;
        private Double limiteSuperior;

        CategoriaCliente(String descricao, Double limiteInferior, Double limiteSuperior) {
            this.descricao = descricao;
            this.limiteInferior = limiteInferior;
            this.limiteSuperior = limiteSuperior;
        }

        public static CategoriaCliente fromValorTotal(Double valorTotal) {
            if (valorTotal == null) return BRONZE;
            for (CategoriaCliente categoria : values()) {
                if (valorTotal >= categoria.limiteInferior && valorTotal < categoria.limiteSuperior) {
                    return categoria;
                }
            }
            return DIAMANTE;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Métodos de cálculo
    public void calcularTicketMedio() {
        if (totalPedidos != null && totalPedidos > 0 && valorTotalGasto != null) {
            this.ticketMedio = valorTotalGasto.divide(BigDecimal.valueOf(totalPedidos), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.ticketMedio = BigDecimal.ZERO;
        }
    }

    public void calcularDiasDesdeUltimoPedido() {
        if (ultimoPedidoData != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(ultimoPedidoData, LocalDateTime.now());
            this.diasDesdeUltimoPedido = Math.max(0, dias);
        }
    }

    public void determinarCategoria() {
        if (valorTotalGasto != null) {
            this.categoria = CategoriaCliente.fromValorTotal(valorTotalGasto.doubleValue());
        } else {
            this.categoria = CategoriaCliente.BRONZE;
        }
    }

    public void determinarStatusCliente() {
        // Cliente recente: primeiro pedido nos últimos 30 dias
        if (diasDesdeUltimoPedido != null) {
            this.isClienteRecente = diasDesdeUltimoPedido <= 30;
        }

        // Cliente frequente: mais de 5 pedidos ou pedido por mês
        if (totalPedidos != null && diasDesdeUltimoPedido != null && diasDesdeUltimoPedido > 0) {
            double mesesAtivo = diasDesdeUltimoPedido / 30.0;
            this.frequenciaMediaPedidosPorMes = totalPedidos / Math.max(1, mesesAtivo);
            this.isClienteFrequente = this.frequenciaMediaPedidosPorMes >= 1.0;
        } else {
            this.isClienteFrequente = false;
        }

        // Cliente premium: valor total > 10.000 ou categoria OURO/DIAMANTE
        if (valorTotalGasto != null) {
            this.isClientePremium = valorTotalGasto.doubleValue() > 10000 ||
                    categoria == CategoriaCliente.OURO ||
                    categoria == CategoriaCliente.DIAMANTE;
        }
    }

    public void calcularTudo() {
        calcularTicketMedio();
        calcularDiasDesdeUltimoPedido();
        determinarCategoria();
        determinarStatusCliente();
    }

    public String getResumoCompleto() {
        return String.format("Cliente: %s (CNPJ: %s) - %s - Total gasto: R$ %.2f - %d pedidos",
                nome, cnpj, categoria != null ? categoria.getDescricao() : "Bronze",
                valorTotalGasto != null ? valorTotalGasto : BigDecimal.ZERO,
                totalPedidos != null ? totalPedidos : 0);
    }
}