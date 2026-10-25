package com.jubo.cameriere.domain;

import com.jubo.cameriere.api.security.TotpService;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TOTP (RFC 6238)")
class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    @DisplayName("Gera o código do vetor de teste oficial da RFC 6238")
    void quandoVetorDaRfc_deveGerarCodigoEsperado() {
        byte[] chave = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        // T = 59s → passo 1 → 94287082 (8 dígitos); com 6 dígitos: 287082
        assertEquals("287082", totpService.gerarCodigo(chave, 1));
        // T = 1111111109s → passo 37037036 → 07081804 → 081804
        assertEquals("081804", totpService.gerarCodigo(chave, 37037036));
    }

    @Test
    @DisplayName("Aceita o código atual e recusa a reutilização do mesmo passo")
    void quandoCodigoReutilizado_deveRecusar() {
        String segredo = totpService.gerarSegredo();
        long passo = Instant.now().getEpochSecond() / 30;
        String codigo = totpService.gerarCodigo(new Base32().decode(segredo), passo);

        OptionalLong aceito = totpService.verificar(segredo, codigo, null);
        assertTrue(aceito.isPresent());
        assertTrue(totpService.verificar(segredo, codigo, aceito.getAsLong()).isEmpty());
    }

    @Test
    @DisplayName("Recusa código malformado ou expirado")
    void quandoCodigoInvalido_deveRecusar() {
        String segredo = totpService.gerarSegredo();
        assertTrue(totpService.verificar(segredo, "12345", null).isEmpty());
        assertTrue(totpService.verificar(segredo, "abcdef", null).isEmpty());
        assertTrue(totpService.verificar(segredo, null, null).isEmpty());

        long passoAntigo = Instant.now().getEpochSecond() / 30 - 10;
        String expirado = totpService.gerarCodigo(new Base32().decode(segredo), passoAntigo);
        OptionalLong resultado = totpService.verificar(segredo, expirado, null);
        // Só passaria se, por coincidência, o código antigo fosse igual a um dos 3 válidos agora
        assertTrue(resultado.isEmpty() || resultado.getAsLong() != passoAntigo);
    }

    @Test
    @DisplayName("Monta a URI otpauth lida pelos aplicativos autenticadores")
    void quandoMontarUri_deveSeguirFormatoOtpauth() {
        String uri = totpService.uriOtpAuth("Jubo Pedidos", "joao@teste.com", "ABC");
        assertEquals("otpauth://totp/Jubo%20Pedidos%3Ajoao%40teste.com?secret=ABC&issuer=Jubo%20Pedidos"
                + "&algorithm=SHA1&digits=6&period=30", uri);
    }
}
