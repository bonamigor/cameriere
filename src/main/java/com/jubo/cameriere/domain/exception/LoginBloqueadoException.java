package com.jubo.cameriere.domain.exception;

import org.springframework.http.HttpStatus;

public class LoginBloqueadoException extends NegocioException {

    public LoginBloqueadoException(long minutos) {
        super("Muitas tentativas de login. Tente novamente em " + minutos
                + (minutos == 1 ? " minuto." : " minutos."), HttpStatus.TOO_MANY_REQUESTS);
    }
}
