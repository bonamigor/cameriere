package com.jubo.cameriere.domain.exception;

public class CodigoMfaInvalidoException extends NegocioException {

    public CodigoMfaInvalidoException() {
        super("Código inválido. Confira se o horário do celular está correto e tente novamente.");
    }
}
