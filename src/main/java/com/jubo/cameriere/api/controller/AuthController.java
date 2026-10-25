package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.dto.AtivarMfaRequestDTO;
import com.jubo.cameriere.api.dto.CodigosRecuperacaoDTO;
import com.jubo.cameriere.api.dto.ConfiguracaoMfaDTO;
import com.jubo.cameriere.api.dto.DesativarMfaRequestDTO;
import com.jubo.cameriere.api.dto.LoginMfaRequestDTO;
import com.jubo.cameriere.api.dto.LoginRequestDTO;
import com.jubo.cameriere.api.dto.LoginResponseDTO;
import com.jubo.cameriere.api.dto.SessaoDTO;
import com.jubo.cameriere.api.security.AuthCookieService;
import com.jubo.cameriere.api.security.AuthService;
import com.jubo.cameriere.api.security.MfaService;
import com.jubo.cameriere.api.security.UsuarioAutenticado;
import com.jubo.cameriere.domain.exception.NegocioException;
import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final MfaService mfaService;
    private final AuditoriaService auditoriaService;

    /** Retorna a sessão (com cookie) ou {@code mfaPendente: true} quando falta o código 2FA. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest http) {
        AuthService.ResultadoLogin resultado = authService.autenticar(request.email(), request.senha(), http.getRemoteAddr());
        if (resultado instanceof AuthService.MfaPendente pendente) {
            return ResponseEntity.ok(pendente.resposta());
        }
        return comCookie(((AuthService.Autenticado) resultado).resposta());
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<LoginResponseDTO> loginSegundoFator(@Valid @RequestBody LoginMfaRequestDTO request,
                                                              HttpServletRequest http) {
        return comCookie(authService.concluirSegundoFator(request.mfaToken(), request.codigo(), http.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpServletRequest http) {
        if (usuario != null) {
            auditoriaService.registrar(AcaoAuditoria.LOGOUT, usuario.getId(), usuario.getEmail(), http.getRemoteAddr(), null);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.limpar().toString())
                .build();
    }

    @GetMapping("/me")
    public SessaoDTO sessao(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new SessaoDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getClienteId(),
                usuario.isTotpAtivo(),
                usuario.isTotpAtivo() ? mfaService.codigosRecuperacaoRestantes(usuario.getId()) : 0
        );
    }

    @PostMapping("/2fa/configurar")
    public ConfiguracaoMfaDTO configurarMfa(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mfaService.iniciarConfiguracao(usuario.getId());
    }

    @PostMapping("/2fa/ativar")
    public CodigosRecuperacaoDTO ativarMfa(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @Valid @RequestBody AtivarMfaRequestDTO request,
                                           HttpServletRequest http) {
        try {
            CodigosRecuperacaoDTO codigos = new CodigosRecuperacaoDTO(mfaService.ativar(usuario.getId(), request.codigo()));
            auditoriaService.registrar(AcaoAuditoria.MFA_ATIVADO, usuario.getId(), usuario.getEmail(), http.getRemoteAddr(), null);
            return codigos;
        } catch (NegocioException e) {
            auditoriaService.registrar(AcaoAuditoria.MFA_FALHA, usuario.getId(), usuario.getEmail(), http.getRemoteAddr(), "Ativação: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/2fa/desativar")
    public ResponseEntity<Void> desativarMfa(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                             @Valid @RequestBody DesativarMfaRequestDTO request,
                                             HttpServletRequest http) {
        try {
            mfaService.desativar(usuario.getId(), request.senha(), request.codigo());
        } catch (NegocioException e) {
            auditoriaService.registrar(AcaoAuditoria.MFA_FALHA, usuario.getId(), usuario.getEmail(), http.getRemoteAddr(), "Desativação: " + e.getMessage());
            throw e;
        }
        auditoriaService.registrar(AcaoAuditoria.MFA_DESATIVADO, usuario.getId(), usuario.getEmail(), http.getRemoteAddr(), null);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<LoginResponseDTO> comCookie(LoginResponseDTO response) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieService.criar(response.accessToken()).toString())
                .body(response);
    }
}
