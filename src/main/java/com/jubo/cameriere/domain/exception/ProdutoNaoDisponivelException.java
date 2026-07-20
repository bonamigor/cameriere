package com.jubo.cameriere.domain.exception;

public class ProdutoNaoDisponivelException extends NegocioException {
    public ProdutoNaoDisponivelException(String message) {
        super(message);
    }
}
