package com.jubo.cameriere.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

    @DecimalMin(value = "0", message = "Quantidade em estoque não pode ser negativa")
    @Digits(integer = 9, fraction = 3, message = "Quantidade em estoque aceita até 3 casas decimais")
    private BigDecimal quantidadeEstoque;

    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo;

    @Min(value = 0, message = "Estoque máximo não pode ser negativo")
    private Integer estoqueMaximo;

    private String localizacaoPrateleira;

    private Boolean ativo;
}