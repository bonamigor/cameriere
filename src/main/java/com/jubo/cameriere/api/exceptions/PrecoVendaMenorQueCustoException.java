package com.jubo.cameriere.api.exceptions;

public class PrecoVendaMenorQueCustoException extends RuntimeException {
    public PrecoVendaMenorQueCustoException(String message) {
        super(message);
    }
}