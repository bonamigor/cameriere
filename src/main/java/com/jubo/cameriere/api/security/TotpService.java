package com.jubo.cameriere.api.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.OptionalLong;

/** TOTP (RFC 6238): SHA-1, 6 dígitos, passos de 30s — o padrão dos apps autenticadores. */
@Service
public class TotpService {

    private static final int PERIODO_SEGUNDOS = 30;
    /** Aceita o passo anterior e o seguinte para tolerar relógios levemente fora de sincronia. */
    private static final int TOLERANCIA_PASSOS = 1;

    private final SecureRandom random = new SecureRandom();

    public String gerarSegredo() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes).replace("=", "");
    }

    public String uriOtpAuth(String emissor, String conta, String segredo) {
        return "otpauth://totp/" + codificar(emissor + ":" + conta)
                + "?secret=" + segredo
                + "&issuer=" + codificar(emissor)
                + "&algorithm=SHA1&digits=6&period=" + PERIODO_SEGUNDOS;
    }

    /**
     * Retorna o passo de tempo do código aceito, ou vazio se inválido. Passos iguais ou
     * anteriores a {@code ultimoPassoUsado} são recusados, impedindo reutilizar um código.
     */
    public OptionalLong verificar(String segredo, String codigo, Long ultimoPassoUsado) {
        if (codigo == null || !codigo.matches("\\d{6}")) {
            return OptionalLong.empty();
        }
        byte[] chave = new Base32().decode(segredo);
        long passoAtual = Instant.now().getEpochSecond() / PERIODO_SEGUNDOS;
        for (long passo = passoAtual - TOLERANCIA_PASSOS; passo <= passoAtual + TOLERANCIA_PASSOS; passo++) {
            if (ultimoPassoUsado != null && passo <= ultimoPassoUsado) {
                continue;
            }
            byte[] esperado = gerarCodigo(chave, passo).getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(esperado, codigo.getBytes(StandardCharsets.US_ASCII))) {
                return OptionalLong.of(passo);
            }
        }
        return OptionalLong.empty();
    }

    public String gerarCodigo(byte[] chave, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(chave, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int offset = hash[hash.length - 1] & 0x0f;
            int binario = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return String.format("%06d", binario % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 indisponível", e);
        }
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
