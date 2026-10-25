package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.EstanteProdutoRequestDTO;
import com.jubo.cameriere.api.dto.EstanteProdutoResponseDTO;
import com.jubo.cameriere.api.dto.EstanteProdutoUpdateDTO;
import com.jubo.cameriere.domain.model.EstanteProduto;
import com.jubo.cameriere.domain.model.UnidadeMedida;
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
        UnidadeMedida unidade = ep.getProduto().getUnidadeMedida();
        return EstanteProdutoResponseDTO.builder()
                .id(ep.getId())
                .estanteId(ep.getEstante().getId())
                .estantePeriodo(ep.getEstante().getPeriodo())
                .produtoId(ep.getProduto().getId())
                .produtoNome(ep.getProduto().getNome())
                .unidadeMedidaSigla(unidade != null ? unidade.getSigla() : null)
                .unidadeMedidaDescricao(unidade != null ? unidade.getDescricao() : null)
                .precoVenda(ep.getPrecoVenda())
                .quantidade(ep.getQuantidade().stripTrailingZeros())
                .ativo(ep.getAtivo())
                .dataCadastro(ep.getDataCadastro())
                .dataAtualizacao(ep.getDataAtualizacao())
                .disponivel(ep.isDisponivel())
                .build();
    }
}
