package com.jubo.cameriere.domain.mapper;

import com.jubo.cameriere.domain.dto.PedidoResponseDTO;
import com.jubo.cameriere.domain.model.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// ProdutoMapper.java (classe auxiliar)
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    @Mapping(target = "unidadeMedida", source = "unidadeMedida.sigla")
    PedidoResponseDTO.ItemPedidoResponseDTO.ProdutoResumoDTO toResumoDTO(Produto produto);
}
