package com.jubo.cameriere.api.security;

import com.jubo.cameriere.domain.exception.LoginBloqueadoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueio temporário contra força bruta, por e-mail e por IP. Fica em memória:
 * cada fornecedor roda uma única instância da API, então não há estado a compartilhar.
 */
@Service
public class TentativasLoginService {

    static final int MAX_FALHAS_POR_EMAIL = 5;
    static final Duration JANELA = Duration.ofMinutes(15);

    /**
     * 0 desativa o limite por IP. Necessário quando outro proxy (ex.: Vercel) fica na frente da API
     * e todas as requisições chegam com o mesmo IP — o limite por e-mail continua valendo.
     */
    private final int maxFalhasPorIp;

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public TentativasLoginService(@Value("${cameriere.security.login.max-falhas-por-ip:20}") int maxFalhasPorIp) {
        this.maxFalhasPorIp = maxFalhasPorIp;
    }

    public void verificarBloqueio(String email, String ip) {
        Instant agora = Instant.now();
        verificar(chaveEmail(email), MAX_FALHAS_POR_EMAIL, agora);
        if (maxFalhasPorIp > 0) {
            verificar(chaveIp(ip), maxFalhasPorIp, agora);
        }
    }

    public void registrarFalha(String email, String ip) {
        Instant agora = Instant.now();
        incrementar(chaveEmail(email), agora);
        incrementar(chaveIp(ip), agora);
    }

    public void registrarSucesso(String email) {
        registros.remove(chaveEmail(email));
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void limparExpirados() {
        Instant agora = Instant.now();
        registros.values().removeIf(r -> r.expirado(agora));
    }

    private void verificar(String chave, int max, Instant agora) {
        Registro registro = registros.get(chave);
        if (registro != null && !registro.expirado(agora) && registro.falhas >= max) {
            long minutos = Math.max(1, Duration.between(agora, registro.inicio.plus(JANELA)).toMinutes() + 1);
            throw new LoginBloqueadoException(minutos);
        }
    }

    private void incrementar(String chave, Instant agora) {
        registros.compute(chave, (k, atual) -> {
            if (atual == null || atual.expirado(agora)) {
                return new Registro(agora, 1);
            }
            return new Registro(atual.inicio, atual.falhas + 1);
        });
    }

    private static String chaveEmail(String email) {
        return "email:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
    }

    private static String chaveIp(String ip) {
        return "ip:" + ip;
    }

    private record Registro(Instant inicio, int falhas) {
        boolean expirado(Instant agora) {
            return agora.isAfter(inicio.plus(JANELA));
        }
    }
}
