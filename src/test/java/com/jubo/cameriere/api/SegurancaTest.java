package com.jubo.cameriere.api;

import com.jubo.cameriere.api.security.TotpService;
import com.jubo.cameriere.domain.model.Usuario;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API Segurança: bloqueio de login, 2FA e auditoria")
class SegurancaTest extends ApiAuthSupport {

    private static final String SENHA = "Senha@2026";

    @Autowired
    private TotpService totpService;

    @Test
    @DisplayName("Após 5 senhas erradas o e-mail fica bloqueado (429), mesmo com a senha certa")
    void quandoMuitasFalhas_deveBloquearLogin() {
        String email = criarUsuario(false).getEmail();
        for (int i = 0; i < 5; i++) {
            login(email, "senha-errada").then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
        login(email, SENHA).then()
                .statusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                .body("userMessage", containsString("Muitas tentativas"));
    }

    @Test
    @DisplayName("Fluxo completo de 2FA: configurar, ativar, login em duas etapas e código de recuperação")
    void quandoMfaAtivo_deveExigirSegundoFator() {
        Usuario usuario = criarUsuario(true);
        String token = autenticar(usuario.getEmail(), SENHA);

        String segredo = givenBearer(token).when().post("/api/auth/2fa/configurar")
                .then().statusCode(200)
                .body("otpauthUri", startsWith("otpauth://totp/"))
                .extract().path("segredo");

        List<String> codigos = givenBearer(token)
                .body("{\"codigo\": \"%s\"}".formatted(codigoAtual(segredo)))
                .when().post("/api/auth/2fa/ativar")
                .then().statusCode(200)
                .body("codigos", hasSize(8))
                .extract().path("codigos");

        // Primeira etapa: senha certa não abre sessão, só devolve o token de verificação
        Response primeiraEtapa = login(usuario.getEmail(), SENHA);
        primeiraEtapa.then().statusCode(200)
                .body("mfaPendente", equalTo(true))
                .body("accessToken", nullValue())
                .header("Set-Cookie", nullValue());
        String mfaToken = primeiraEtapa.path("mfaToken");

        // O token de verificação não serve como credencial da API
        givenBearer(mfaToken).when().get("/api/clientes").then().statusCode(HttpStatus.UNAUTHORIZED.value());

        segundaEtapa(mfaToken, "000000").then().statusCode(HttpStatus.BAD_REQUEST.value());

        segundaEtapa(mfaToken, codigos.get(0)).then()
                .statusCode(200)
                .body("doisFatoresAtivo", equalTo(true))
                .header("Set-Cookie", startsWith("cameriere_token="));

        // Código de recuperação é de uso único
        segundaEtapa(mfaToken, codigos.get(0)).then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Admin redefine o 2FA de um usuário, que volta a entrar só com a senha")
    void quandoAdminRedefinirMfa_usuarioEntraSemSegundoFator() {
        Usuario usuario = criarUsuario(false);
        String token = autenticar(usuario.getEmail(), SENHA);
        String segredo = givenBearer(token).post("/api/auth/2fa/configurar").path("segredo");
        givenBearer(token).body("{\"codigo\": \"%s\"}".formatted(codigoAtual(segredo)))
                .post("/api/auth/2fa/ativar").then().statusCode(200);

        login(usuario.getEmail(), SENHA).then().body("mfaPendente", equalTo(true));

        givenAdmin().when().delete("/api/usuarios/{id}/2fa", usuario.getId())
                .then().statusCode(HttpStatus.NO_CONTENT.value());

        login(usuario.getEmail(), SENHA).then().statusCode(200).body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Falha de login fica registrada na auditoria, visível só para ADMIN")
    void quandoLoginFalhar_deveRegistrarAuditoria() {
        String email = emailUnico("auditado");
        login(email, "qualquer").then().statusCode(HttpStatus.UNAUTHORIZED.value());

        givenAdmin().queryParam("email", email).queryParam("acao", "LOGIN_FALHA")
                .when().get("/api/auditoria")
                .then().statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].usuarioEmail", equalTo(email))
                .body("content[0].ip", notNullValue());

        givenUser().when().get("/api/auditoria").then().statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Alterações pela API entram na auditoria com usuário, método e status")
    void quandoAlterarRecurso_deveRegistrarAuditoria() {
        givenAdmin().body("{\"sigla\": \"%s\", \"descricao\": \"Teste auditoria\"}".formatted(siglaUnica()))
                .post("/api/unidades-medida");

        givenAdmin().queryParam("email", ADMIN_EMAIL).queryParam("acao", "ALTERACAO")
                .when().get("/api/auditoria")
                .then().statusCode(200)
                .body("content[0].metodo", equalTo("POST"))
                .body("content[0].recurso", equalTo("/api/unidades-medida"));
    }

    private Usuario criarUsuario(boolean admin) {
        Usuario usuario = new Usuario(null, "Usuário Segurança", emailUnico("seg"),
                passwordEncoder.encode(SENHA), admin, null);
        return usuarioRepository.save(usuario);
    }

    private Response login(String email, String senha) {
        return givenSemAuth()
                .body("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha))
                .post("/api/auth/login");
    }

    private Response segundaEtapa(String mfaToken, String codigo) {
        return givenSemAuth()
                .body("{\"mfaToken\": \"%s\", \"codigo\": \"%s\"}".formatted(mfaToken, codigo))
                .post("/api/auth/login/2fa");
    }

    private io.restassured.specification.RequestSpecification givenBearer(String token) {
        return given().header("Authorization", "Bearer " + token).contentType("application/json");
    }

    private String codigoAtual(String segredo) {
        long passo = Instant.now().getEpochSecond() / 30;
        String codigo = totpService.gerarCodigo(new Base32().decode(segredo), passo);
        assertEquals(6, codigo.length());
        return codigo;
    }
}
