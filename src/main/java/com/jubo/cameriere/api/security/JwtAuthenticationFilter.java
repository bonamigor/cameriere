package com.jubo.cameriere.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jubo.cameriere.api.exceptionhandler.Problem;
import com.jubo.cameriere.api.exceptionhandler.ProblemType;
import com.jubo.cameriere.config.security.MfaProperties;
import com.jubo.cameriere.domain.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthCookieService authCookieService;
    private final MfaProperties mfaProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Header Bearer (Swagger, Postman, testes) tem prioridade; o frontend usa o cookie HttpOnly
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authCookieService.extrairToken(request).orElse(null);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsuarioAutenticado usuario = (UsuarioAutenticado) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, usuario)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        if (exigeConfigurarMfa(request)) {
            responderMfaObrigatorio(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Administrador sem 2FA só acessa /api/auth/** (onde configura o 2FA) até ativá-lo. */
    private boolean exigeConfigurarMfa(HttpServletRequest request) {
        if (!mfaProperties.obrigatorioParaAdmin()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getPrincipal() instanceof UsuarioAutenticado usuario
                && usuario.getRole() == Role.ADMIN
                && !usuario.isTotpAtivo()
                && !request.getRequestURI().startsWith("/api/auth/");
    }

    private void responderMfaObrigatorio(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Problem problem = Problem.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(OffsetDateTime.now())
                .type(ProblemType.MFA_OBRIGATORIO.getUri())
                .title(ProblemType.MFA_OBRIGATORIO.getTitle())
                .detail("Administradores precisam ativar a verificação em duas etapas.")
                .userMessage("Ative a verificação em duas etapas para continuar usando o sistema.")
                .build();
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
