package com.jubo.cameriere.api.exceptions;

public class ProdutoJaAssociadoException extends RuntimeException {
    public ProdutoJaAssociadoException(String message) {
        super(message);
    }
}