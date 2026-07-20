package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.EstanteRequestDTO;
import com.jubo.cameriere.api.dto.EstanteResponseDTO;
import com.jubo.cameriere.api.dto.EstanteUpdateDTO;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.service.comando.EstanteCreateCommand;
import com.jubo.cameriere.domain.service.comando.EstanteUpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class EstanteAssembler {

    public EstanteCreateCommand toCreateCommand(EstanteRequestDTO request) {
        return new EstanteCreateCommand(
                request.getPeriodo(),
                request.getObservacao(),
                request.getClienteId(),
                request.getAtiva(),
                request.getEmpresa()
        );
    }

    public EstanteUpdateCommand toUpdateCommand(EstanteUpdateDTO updateDTO) {
        return new EstanteUpdateCommand(
                updateDTO.getPeriodo(),
                updateDTO.getObservacao(),
                updateDTO.getAtiva(),
                updateDTO.getEmpresa()
        );
    }

    public EstanteResponseDTO toResponseDTO(Estante estante) {
        if (estante == null) {
            return null;
        }

        int quantidadeProdutos = estante.getProdutos() != null ? estante.getProdutos().size() : 0;

        return EstanteResponseDTO.builder()
                .id(estante.getId())
                .periodo(estante.getPeriodo())
                .observacao(estante.getObservacao())
                .clienteId(estante.getCliente().getId())
                .clienteNome(estante.getCliente().getNome())
                .clienteCnpj(estante.getCliente().getCnpj())
                .ativa(estante.getAtiva())
                .empresa(estante.getEmpresa())
                .quantidadeProdutos(quantidadeProdutos)
                .build();
    }
}
