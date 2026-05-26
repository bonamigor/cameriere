package com.jubo.cameriere.domain.model;

public enum StatusPedido {
    PENDENTE("Pendente"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado"),
    ENTREGUE("Entregue");

    private String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }
}