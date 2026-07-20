package com.jubo.cameriere.api.dto;

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

}