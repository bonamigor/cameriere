package com.jubo.cameriere.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstanteProdutoUpdateDTO {

    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    private BigDecimal precoVenda;

    @Min(value = 0, message = "Quantidade em estoque não pode ser negativa")
    private Integer quantidadeEstoque;

    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo;

    @Min(value = 0, message = "Estoque máximo não pode ser negativo")
    private Integer estoqueMaximo;

    private String localizacaoPrateleira;

    private Boolean ativo;
}