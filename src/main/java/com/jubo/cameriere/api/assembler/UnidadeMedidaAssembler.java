package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.UnidadeMedidaRequestDTO;
import com.jubo.cameriere.api.dto.UnidadeMedidaResponseDTO;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import org.springframework.stereotype.Component;

@Component
public class UnidadeMedidaAssembler {

    public UnidadeMedida toEntity(UnidadeMedidaRequestDTO request) {
        UnidadeMedida unidade = new UnidadeMedida();
        copyToEntity(request, unidade);
        return unidade;
    }

    public void copyToEntity(UnidadeMedidaRequestDTO request, UnidadeMedida unidade) {
        unidade.setSigla(request.sigla());
        unidade.setDescricao(request.descricao());
    }

    public UnidadeMedidaResponseDTO toResponseDTO(UnidadeMedida unidade) {
        return new UnidadeMedidaResponseDTO(
                unidade.getId(),
                unidade.getSigla(),
                unidade.getDescricao(),
                unidade.getDataCriacao(),
                unidade.getDataAtualizacao()
        );
    }
}
