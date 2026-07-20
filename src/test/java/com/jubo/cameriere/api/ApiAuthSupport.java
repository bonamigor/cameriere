package com.jubo.cameriere.api;

import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.repository.UsuarioRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiAuthSupport {

    protected static final String ADMIN_EMAIL = "admin@cafeteriacentral.com";
    protected static final String OPERADOR_EMAIL = "operador@cafeteriacentral.com";
    protected static final String SENHA_PADRAO = "123456";

    @LocalServerPort
    protected int port;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String adminToken;
    protected String userToken;

    @BeforeEach
    void setUpAuth() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        garantirUsuario(ADMIN_EMAIL, "Admin Teste", true);
        garantirUsuario(OPERADOR_EMAIL, "Operador Teste", false);

        adminToken = autenticar(ADMIN_EMAIL, SENHA_PADRAO);
        userToken = autenticar(OPERADOR_EMAIL, SENHA_PADRAO);
    }

    protected RequestSpecification givenAdmin() {
        return given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON);
    }

    protected RequestSpecification givenUser() {
        return given()
                .header("Authorization", "Bearer " + userToken)
                .contentType(ContentType.JSON);
    }

    protected RequestSpecification givenSemAuth() {
        return given().contentType(ContentType.JSON);
    }

    protected String autenticar(String email, String senha) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "senha": "%s"
                        }
                        """.formatted(email, senha))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    protected String cnpjUnico() {
        String digits = String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 100_000_000_000_000L));
        return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5, 8)
                + "/" + digits.substring(8, 12) + "-" + digits.substring(12, 14);
    }

    protected String emailUnico(String prefixo) {
        return prefixo + "+" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
    }

    protected String siglaUnica() {
        String base = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return base;
    }

    private void garantirUsuario(String email, String nome, boolean admin) {
        usuarioRepository.findByEmail(email).ifPresentOrElse(
                existente -> {
                    existente.setSenha(passwordEncoder.encode(SENHA_PADRAO));
                    existente.setAdmin(admin);
                    existente.setNome(nome);
                    usuarioRepository.save(existente);
                },
                () -> {
                    Usuario usuario = new Usuario();
                    usuario.setNome(nome);
                    usuario.setEmail(email);
                    usuario.setSenha(passwordEncoder.encode(SENHA_PADRAO));
                    usuario.setAdmin(admin);
                    usuarioRepository.save(usuario);
                }
        );
    }
}
