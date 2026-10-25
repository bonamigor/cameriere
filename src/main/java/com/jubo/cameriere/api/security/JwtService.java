package com.jubo.cameriere.api.security;

import com.jubo.cameriere.config.security.JwtProperties;
import com.jubo.cameriere.domain.exception.NegocioException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_TIPO = "typ";
    private static final String TIPO_ACESSO = "access";
    private static final String TIPO_MFA = "mfa";
    private static final long MFA_TOKEN_VALIDADE_MS = 5 * 60 * 1000;
    private static final String MFA_TOKEN_INVALIDO = "A verificação expirou. Faça login novamente.";

    private final JwtProperties jwtProperties;

    public String generateToken(UsuarioAutenticado usuario) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("role", usuario.getRole().name())
                .claim("clienteId", usuario.getClienteId())
                .claim("authorities", usuario.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim(CLAIM_TIPO, TIPO_ACESSO)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey())
                .compact();
    }

    /** Token de curta duração que só comprova a senha; não dá acesso à API. */
    public String generateMfaToken(UsuarioAutenticado usuario) {
        Date now = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim(CLAIM_TIPO, TIPO_MFA)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + MFA_TOKEN_VALIDADE_MS))
                .signWith(signingKey())
                .compact();
    }

    /** Retorna o e-mail do token de verificação, ou lança se estiver inválido ou expirado. */
    public String extractMfaSubject(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!TIPO_MFA.equals(claims.get(CLAIM_TIPO, String.class))) {
                throw new NegocioException(MFA_TOKEN_INVALIDO);
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new NegocioException(MFA_TOKEN_INVALIDO);
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UsuarioAutenticado usuario) {
        Claims claims = parseClaims(token);
        return TIPO_ACESSO.equals(claims.get(CLAIM_TIPO, String.class))
                && claims.getSubject().equals(usuario.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
