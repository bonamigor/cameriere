package com.jubo.cameriere.api.dto;

/** Senha correta, mas ainda falta o código do aplicativo autenticador. */
public record LoginMfaPendenteDTO(
        boolean mfaPendente,
        String mfaToken
) {
}
