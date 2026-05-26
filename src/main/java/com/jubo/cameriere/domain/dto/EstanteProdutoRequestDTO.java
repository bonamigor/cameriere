package com.jubo.cameriere.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstanteProdutoRequestDTO {

    @NotNull(message = "ID da estante é obrigatório")
    private Long estanteId;

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "Preço de venda excede o limite permitido")
    private BigDecimal precoVenda;

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @Min(value = 0, message = "Quantidade em estoque não pode ser negativa")
    @Max(value = 999999, message = "Quantidade em estoque excede o limite")
    private Integer quantidadeEstoque;

    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    private Integer estoqueMinimo;

    @Min(value = 0, message = "Estoque máximo não pode ser negativo")
    private Integer estoqueMaximo;

    private String localizacaoPrateleira;
}