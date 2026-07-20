package com.jubo.cameriere.config.security;

import com.jubo.cameriere.api.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jubo.cameriere.api.exceptionhandler.Problem;
import com.jubo.cameriere.api.exceptionhandler.ProblemType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.OffsetDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/pedidos/relatorios/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/empresas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/empresas/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/unidades-medida/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/unidades-medida/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/unidades-medida/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/estantes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/estantes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/estantes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/estantes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/estantes-produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/estantes-produtos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/estantes-produtos/**").hasRole("ADMIN")

                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Problem problem = Problem.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .timestamp(OffsetDateTime.now())
                    .type(ProblemType.NAO_AUTENTICADO.getUri())
                    .title(ProblemType.NAO_AUTENTICADO.getTitle())
                    .detail("É necessário autenticar-se para acessar este recurso.")
                    .userMessage("Faça login e envie o token Bearer no header Authorization.")
                    .build();
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Problem problem = Problem.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .timestamp(OffsetDateTime.now())
                    .type(ProblemType.ACESSO_NEGADO.getUri())
                    .title(ProblemType.ACESSO_NEGADO.getTitle())
                    .detail("Você não possui permissão para acessar este recurso.")
                    .userMessage("Seu perfil não permite executar esta operação.")
                    .build();
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }
}
