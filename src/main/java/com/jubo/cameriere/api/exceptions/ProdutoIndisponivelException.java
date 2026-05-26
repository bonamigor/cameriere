package com.jubo.cameriere.api.exceptions;

public class ProdutoIndisponivelException extends RuntimeException {
    public ProdutoIndisponivelException(String message) {
        super(message);
    }
}
