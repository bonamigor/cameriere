package com.jubo.cameriere.api.security;

import com.jubo.cameriere.config.security.MfaProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/** AES-256-GCM para dados sensíveis em repouso (ex.: segredos TOTP). */
@Service
public class CriptografiaService {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey chave;
    private final SecureRandom random = new SecureRandom();

    public CriptografiaService(MfaProperties mfaProperties) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(mfaProperties.chaveCriptografia());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("MFA_ENCRYPTION_KEY deve estar em Base64", e);
        }
        if (bytes.length != 32) {
            throw new IllegalStateException("MFA_ENCRYPTION_KEY deve ter 32 bytes (gere com: openssl rand -base64 32)");
        }
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    public String criptografar(String texto) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + cifrado.length).put(iv).put(cifrado).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao criptografar", e);
        }
    }

    public String descriptografar(String base64) {
        try {
            byte[] dados = Base64.getDecoder().decode(base64);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, dados, 0, IV_BYTES));
            return new String(cipher.doFinal(dados, IV_BYTES, dados.length - IV_BYTES), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Falha ao descriptografar", e);
        }
    }
}
