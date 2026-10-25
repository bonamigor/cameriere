package com.jubo.cameriere.api.dto;

/** Segredo exibido uma única vez para cadastro manual e a URI usada no QR code. */
public record ConfiguracaoMfaDTO(
        String segredo,
        String otpauthUri
) {
}
