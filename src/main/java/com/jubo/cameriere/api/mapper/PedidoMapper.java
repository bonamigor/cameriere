package com.jubo.cameriere.api.mapper;
import com.jubo.cameriere.api.dto.PedidoRequestDTO;
import com.jubo.cameriere.api.dto.PedidoResponseDTO;
import com.jubo.cameriere.domain.model.Pedido;
import com.jubo.cameriere.domain.model.Produto;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "spring",
        imports = {LocalDateTime.class, BigDecimal.class},
        uses = {ProdutoMapper.class})
public interface PedidoMapper {

    // Conversão Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "dataCriacao", expression = "java(LocalDateTime.now())")
    @Mapping(target = "dataConfirmacao", ignore = true)
    @Mapping(target = "dataPrevisaoEntrega", ignore = true)
    @Mapping(target = "dataCancelamento", ignore = true)
    @Mapping(target = "dataEntrega", ignore = true)
    @Mapping(target = "valorTotal", constant = "0")
    @Mapping(target = "entregue", constant = "false")
    @Mapping(target = "finalizado", constant = "false")
    @Mapping(target = "obsCancelamento", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "itens", ignore = true)
    Pedido toEntity(PedidoRequestDTO requestDTO);

    // Conversão Entity -> Response
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "itens", source = "itens")
    @Mapping(target = "status", source = "status")
    PedidoResponseDTO toResponseDTO(Pedido pedido);

    // Lista de Pedidos -> Lista de Response
    List<PedidoResponseDTO> toResponseDTOList(List<Pedido> pedidos);

    // Método auxiliar para converter timestamp para LocalDateTime
    default LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // Método auxiliar para converter LocalDateTime para timestamp
    default Long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Update methods para patches
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "itens", ignore = true)
    void updatePedidoFromDTO(PedidoRequestDTO requestDTO, @MappingTarget Pedido pedido);
}

