package com.jubo.cameriere.api.auditoria;

import com.jubo.cameriere.api.security.UsuarioAutenticado;
import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Registra toda alteração feita pela API (quem, o quê, quando, de onde e o resultado).
 * Nunca grava o corpo da requisição, para não duplicar dados pessoais na trilha.
 */
@Component
@RequiredArgsConstructor
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Set<String> METODOS_ALTERACAO = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditoriaService auditoriaService;

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        String uri = request.getRequestURI();
        // Rotas de autenticação já registram eventos específicos (login, 2FA, logout)
        if (!METODOS_ALTERACAO.contains(request.getMethod()) || uri.startsWith("/api/auth/")) {
            return;
        }

        Long usuarioId = null;
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
            usuarioId = usuario.getId();
            email = usuario.getEmail();
        }

        auditoriaService.registrar(AcaoAuditoria.ALTERACAO, usuarioId, email, request.getMethod(), uri,
                response.getStatus(), request.getRemoteAddr(), null);
    }
}
