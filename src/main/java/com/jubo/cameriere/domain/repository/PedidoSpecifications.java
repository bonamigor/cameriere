package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Pedido;
import com.jubo.cameriere.domain.model.StatusPedido;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class PedidoSpecifications {

    private PedidoSpecifications() {
    }

    public static Specification<Pedido> comFiltros(
            Long pedidoId,
            StatusPedido status,
            Long clienteId,
            LocalDateTime inicio,
            LocalDateTime fim) {
        return Specification
                .where(comId(pedidoId))
                .and(comStatus(status))
                .and(comCliente(clienteId))
                .and(aPartirDe(inicio))
                .and(ate(fim));
    }

    public static Specification<Pedido> comId(Long pedidoId) {
        return (root, query, cb) ->
                pedidoId == null ? null : cb.equal(root.get("id"), pedidoId);
    }

    public static Specification<Pedido> comStatus(StatusPedido status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Pedido> comCliente(Long clienteId) {
        return (root, query, cb) ->
                clienteId == null ? null : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<Pedido> aPartirDe(LocalDateTime inicio) {
        return (root, query, cb) ->
                inicio == null ? null : cb.greaterThanOrEqualTo(root.get("dataCriacao"), inicio);
    }

    public static Specification<Pedido> ate(LocalDateTime fim) {
        return (root, query, cb) ->
                fim == null ? null : cb.lessThanOrEqualTo(root.get("dataCriacao"), fim);
    }
}
