package com.jubo.cameriere.api.exceptions;

public class ProdutoInativoException extends RuntimeException {
    public ProdutoInativoException(String message) {
        super(message);
    }
}