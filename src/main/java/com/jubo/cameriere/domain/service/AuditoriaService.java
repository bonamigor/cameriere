package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.model.Auditoria;
import com.jubo.cameriere.domain.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Value("${cameriere.auditoria.retencao-dias:365}")
    private int retencaoDias;

    /**
     * Chamado fora de transações de negócio (controllers, interceptor, AuthService), então o
     * save grava em transação própria e o registro sobrevive mesmo se a operação auditada falhar.
     */
    public void registrar(AcaoAuditoria acao, Long usuarioId, String usuarioEmail,
                          String metodo, String recurso, Integer statusHttp, String ip, String detalhe) {
        try {
            Auditoria auditoria = new Auditoria();
            auditoria.setDataHora(LocalDateTime.now());
            auditoria.setAcao(acao);
            auditoria.setUsuarioId(usuarioId);
            auditoria.setUsuarioEmail(truncar(usuarioEmail, 255));
            auditoria.setMetodo(metodo);
            auditoria.setRecurso(truncar(recurso, 255));
            auditoria.setStatusHttp(statusHttp);
            auditoria.setIp(truncar(ip, 45));
            auditoria.setDetalhe(truncar(detalhe, 255));
            auditoriaRepository.save(auditoria);
        } catch (Exception e) {
            // A auditoria nunca deve derrubar a requisição do usuário
            log.error("Falha ao registrar auditoria {}", acao, e);
        }
    }

    public void registrar(AcaoAuditoria acao, Long usuarioId, String usuarioEmail, String ip, String detalhe) {
        registrar(acao, usuarioId, usuarioEmail, null, null, null, ip, detalhe);
    }

    @Transactional(readOnly = true)
    public Page<Auditoria> buscar(AcaoAuditoria acao, String email, Pageable pageable) {
        String filtroEmail = email == null || email.isBlank() ? null : email.trim();
        return auditoriaRepository.buscar(acao, filtroEmail, pageable);
    }

    /** LGPD: registros não ficam guardados além do prazo de retenção definido. */
    @Scheduled(cron = "${cameriere.auditoria.limpeza-cron:0 30 3 * * *}")
    @Transactional
    public void removerRegistrosExpirados() {
        int removidos = auditoriaRepository.deleteByDataHoraBefore(LocalDateTime.now().minusDays(retencaoDias));
        if (removidos > 0) {
            log.info("Auditoria: {} registros com mais de {} dias removidos", removidos, retencaoDias);
        }
    }

    private static String truncar(String valor, int max) {
        return valor == null || valor.length() <= max ? valor : valor.substring(0, max);
    }
}
