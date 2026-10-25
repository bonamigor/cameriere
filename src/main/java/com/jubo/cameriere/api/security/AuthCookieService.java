package com.jubo.cameriere.api.security;

import com.jubo.cameriere.config.security.AuthCookieProperties;
import com.jubo.cameriere.config.security.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Emite e lê o cookie HttpOnly que carrega o JWT, para que o token
 * nunca fique acessível ao JavaScript do frontend.
 */
@Component
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public ResponseCookie criar(String token) {
        return base(token)
                .maxAge(Duration.ofMillis(jwtProperties.expirationMs()))
                .build();
    }

    public ResponseCookie limpar() {
        return base("")
                .maxAge(Duration.ZERO)
                .build();
    }

    public Optional<String> extrairToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieProperties.name().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(cookieProperties.name(), value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/");
    }
}
