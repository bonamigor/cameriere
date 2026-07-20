package com.jubo.cameriere.api.security;

import com.jubo.cameriere.api.dto.LoginResponseDTO;
import com.jubo.cameriere.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponseDTO autenticar(String email, String senha) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, senha));

        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        String token = jwtService.generateToken(usuario);

        return new LoginResponseDTO(
                token,
                "Bearer",
                jwtProperties.expirationMs(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole().name(),
                usuario.getClienteId()
        );
    }
}
