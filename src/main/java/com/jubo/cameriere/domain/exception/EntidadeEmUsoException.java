package com.jubo.cameriere.domain.exception;

import org.springframework.http.HttpStatus;

public class EntidadeEmUsoException extends NegocioException {

    public EntidadeEmUsoException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
