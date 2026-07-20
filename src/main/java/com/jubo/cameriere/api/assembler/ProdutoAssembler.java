package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.ProdutoRequestDTO;
import com.jubo.cameriere.api.dto.ProdutoResponseDTO;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import org.springframework.stereotype.Component;

@Component
public class ProdutoAssembler {

    public Produto toEntity(ProdutoRequestDTO request) {
        Produto produto = new Produto();
        copyToEntity(request, produto);
        return produto;
    }

    public void copyToEntity(ProdutoRequestDTO request, Produto produto) {
        produto.setNome(request.nome());
        produto.setPrecoCusto(request.precoCusto());
        if (request.ativo() != null) {
            produto.setAtivo(request.ativo());
        }
    }

    public ProdutoResponseDTO toResponseDTO(Produto produto) {
        UnidadeMedida unidade = produto.getUnidadeMedida();
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPrecoCusto(),
                unidade != null ? unidade.getSigla() : null,
                unidade != null ? unidade.getDescricao() : null,
                produto.getAtivo(),
                produto.getDataCriacao(),
                produto.getDataAtualizacao()
        );
    }
}
