package com.jubo.cameriere.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public NegocioException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
