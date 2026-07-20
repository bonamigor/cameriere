package com.jubo.cameriere.api.mapper;

import com.jubo.cameriere.api.dto.EstanteResponseDTO;
import com.jubo.cameriere.domain.model.Estante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EstanteMapper {

    EstanteMapper INSTANCE = Mappers.getMapper(EstanteMapper.class);

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nome", target = "clienteNome")
    @Mapping(source = "cliente.cnpj", target = "clienteCnpj")
    @Mapping(target = "quantidadeProdutos", expression = "java(estante.getProdutos() != null ? estante.getProdutos().size() : 0)")
    EstanteResponseDTO toResponseDTO(Estante estante);
}