package com.jubo.cameriere.api.security;

import com.jubo.cameriere.api.dto.LoginMfaPendenteDTO;
import com.jubo.cameriere.api.dto.LoginResponseDTO;
import com.jubo.cameriere.config.security.JwtProperties;
import com.jubo.cameriere.domain.exception.CodigoMfaInvalidoException;
import com.jubo.cameriere.domain.exception.LoginBloqueadoException;
import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final MfaService mfaService;
    private final TentativasLoginService tentativasLoginService;
    private final AuditoriaService auditoriaService;

    /** Resultado da primeira etapa: sessão pronta ou pendente do código 2FA. */
    public sealed interface ResultadoLogin permits Autenticado, MfaPendente {
    }

    public record Autenticado(LoginResponseDTO resposta) implements ResultadoLogin {
    }

    public record MfaPendente(LoginMfaPendenteDTO resposta) implements ResultadoLogin {
    }

    public ResultadoLogin autenticar(String email, String senha, String ip) {
        verificarBloqueio(email, ip);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));
        } catch (AuthenticationException e) {
            tentativasLoginService.registrarFalha(email, ip);
            auditoriaService.registrar(AcaoAuditoria.LOGIN_FALHA, null, email, ip, "Senha inválida ou usuário inexistente");
            throw e;
        }

        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        if (usuario.isTotpAtivo()) {
            return new MfaPendente(new LoginMfaPendenteDTO(true, jwtService.generateMfaToken(usuario)));
        }

        tentativasLoginService.registrarSucesso(email);
        auditoriaService.registrar(AcaoAuditoria.LOGIN_SUCESSO, usuario.getId(), usuario.getEmail(), ip, null);
        return new Autenticado(montarResposta(usuario));
    }

    public LoginResponseDTO concluirSegundoFator(String mfaToken, String codigo, String ip) {
        String email = jwtService.extractMfaSubject(mfaToken);
        verificarBloqueio(email, ip);

        UsuarioAutenticado usuario = (UsuarioAutenticado) userDetailsService.loadUserByUsername(email);
        if (!mfaService.verificarCodigoLogin(usuario.getId(), codigo)) {
            tentativasLoginService.registrarFalha(email, ip);
            auditoriaService.registrar(AcaoAuditoria.MFA_FALHA, usuario.getId(), email, ip, "Código inválido no login");
            throw new CodigoMfaInvalidoException();
        }

        tentativasLoginService.registrarSucesso(email);
        auditoriaService.registrar(AcaoAuditoria.LOGIN_SUCESSO, usuario.getId(), email, ip, "Com verificação em duas etapas");
        return montarResposta(usuario);
    }

    private void verificarBloqueio(String email, String ip) {
        try {
            tentativasLoginService.verificarBloqueio(email, ip);
        } catch (LoginBloqueadoException e) {
            auditoriaService.registrar(AcaoAuditoria.LOGIN_BLOQUEADO, null, email, ip, null);
            throw e;
        }
    }

    private LoginResponseDTO montarResposta(UsuarioAutenticado usuario) {
        return new LoginResponseDTO(
                jwtService.generateToken(usuario),
                "Bearer",
                jwtProperties.expirationMs(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getClienteId(),
                usuario.isTotpAtivo()
        );
    }
}
