package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.dto.AuditoriaResponseDTO;
import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.model.Auditoria;
import com.jubo.cameriere.domain.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public Page<AuditoriaResponseDTO> listar(
            @RequestParam(required = false) AcaoAuditoria acao,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 50, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return auditoriaService.buscar(acao, email, pageable).map(AuditoriaController::toDTO);
    }

    private static AuditoriaResponseDTO toDTO(Auditoria a) {
        return new AuditoriaResponseDTO(a.getId(), a.getDataHora(), a.getAcao().name(), a.getUsuarioId(),
                a.getUsuarioEmail(), a.getMetodo(), a.getRecurso(), a.getStatusHttp(), a.getIp(), a.getDetalhe());
    }
}
