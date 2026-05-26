package com.jubo.cameriere.api.exceptions;

public class ProdutoNaoDisponivelException extends RuntimeException {
    public ProdutoNaoDisponivelException(String message) {
        super(message);
    }
}