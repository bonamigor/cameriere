package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.EstanteProdutoRequestDTO;
import com.jubo.cameriere.api.dto.EstanteProdutoResponseDTO;
import com.jubo.cameriere.api.dto.EstanteProdutoUpdateDTO;
import com.jubo.cameriere.domain.model.EstanteProduto;
import com.jubo.cameriere.domain.service.comando.EstanteProdutoAssociateCommand;
import com.jubo.cameriere.domain.service.comando.EstanteProdutoUpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class EstanteProdutoAssembler {

    public EstanteProdutoAssociateCommand toAssociateCommand(EstanteProdutoRequestDTO request) {
        return new EstanteProdutoAssociateCommand(
                request.getEstanteId(),
                request.getProdutoId(),
                request.getPrecoVenda(),
                request.getQuantidadeEstoque()
        );
    }

    public EstanteProdutoUpdateCommand toUpdateCommand(EstanteProdutoUpdateDTO updateDTO) {
        return new EstanteProdutoUpdateCommand(
                updateDTO.getPrecoVenda(),
                updateDTO.getQuantidadeEstoque(),
                updateDTO.getAtivo()
        );
    }

    public EstanteProdutoResponseDTO toResponseDTO(EstanteProduto ep) {
        return EstanteProdutoResponseDTO.builder()
                .id(ep.getId())
                .estanteId(ep.getEstante().getId())
                .estantePeriodo(ep.getEstante().getPeriodo())
                .produtoId(ep.getProduto().getId())
                .produtoNome(ep.getProduto().getNome())
                .precoVenda(ep.getPrecoVenda())
                .quantidade(ep.getQuantidade())
                .ativo(ep.getAtivo())
                .dataCadastro(ep.getDataCadastro())
                .dataAtualizacao(ep.getDataAtualizacao())
                .disponivel(ep.isDisponivel())
                .build();
    }
}
