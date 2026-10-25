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
public class EstanteProdutoResponseDTO {
    private Long id;
    private Long estanteId;
    private String estantePeriodo;
    private Long produtoId;
    private String produtoNome;
    private String unidadeMedidaSigla;
    private String unidadeMedidaDescricao;
    private BigDecimal precoVenda;
    private BigDecimal quantidade;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private Boolean disponivel;
}
