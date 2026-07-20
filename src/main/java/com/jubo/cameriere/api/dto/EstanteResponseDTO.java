package com.jubo.cameriere.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstanteResponseDTO {
    private Long id;
    private String periodo;
    private String observacao;
    private Long clienteId;
    private String clienteNome;
    private String clienteCnpj;
    private Boolean ativa;
    private Integer empresa;
    private Integer quantidadeProdutos;
}