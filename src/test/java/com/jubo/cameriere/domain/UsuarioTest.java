package com.jubo.cameriere.domain;

import com.jubo.cameriere.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import jakarta.validation.*;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    private final Validator validator;

    public UsuarioTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void quandoNomeENulo_deveRetornarErro() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@email.com");
        usuario.setSenha("123456");
        usuario.setAdmin(false);

        Set<String> mensagensErro = validator.validate(usuario).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Nome é obrigatório"));
    }

    @Test
    public void quandoEmailInvalido_deveRetornarErro() {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("email-invalido");
        usuario.setSenha("123456");
        usuario.setAdmin(false);

        Set<String> mensagensErro = validator.validate(usuario).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Email deve ser válido"));
    }

    @Test
    public void quandoSenhaCurta_deveRetornarErro() {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("123");
        usuario.setAdmin(false);

        Set<String> mensagensErro = validator.validate(usuario).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    public void quandoAdminNulo_deveRetornarErro() {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("123456");

        Set<String> mensagensErro = validator.validate(usuario).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Admin é obrigatório"));
    }

    @Test
    public void quandoDadosValidos_devePassarNaValidacao() {
        Usuario usuario = new Usuario();
        usuario.setNome("João Silva");
        usuario.setEmail("joao.silva@empresa.com");
        usuario.setSenha("123456");
        usuario.setAdmin(false);

        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuario);

        assertTrue(violacoes.isEmpty());
    }
}