package com.jubo.cameriere.domain;

import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Endereco;
import org.junit.jupiter.api.Test;
import jakarta.validation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    private final Validator validator;

    public ClienteTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void quandoNomeENulo_deveRetornarErro() {
        Cliente cliente = new Cliente();
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEndereco(new Endereco());

        Set<String> mensagensErro = validator.validate(cliente).stream()
                .map(v -> v.getMessage())
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Nome é obrigatório"));
    }

    @Test
    public void quandoCNPJInvalido_deveRetornarErro() {
        Cliente cliente = new Cliente();
        cliente.setNome("Empresa Teste");
        cliente.setCnpj("12345678901234"); // Sem formatação
        cliente.setEndereco(new Endereco());

        Set<String> mensagensErro = validator.validate(cliente).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        System.out.println(mensagensErro.toString());

        assertTrue(mensagensErro.contains("CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX"));
    }

    @Test
    public void quandoEmailInvalido_deveRetornarErro() {
        Cliente cliente = new Cliente();
        cliente.setNome("Empresa Teste");
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEmail("email-invalido");
        cliente.setEndereco(new Endereco());

        Set<String> mensagensErro = validator.validate(cliente).stream()
                .map(v -> v.getMessage())
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("Email deve ser válido"));
    }

    @Test
    public void quandoCEPInvalido_deveRetornarErro() {
        Endereco endereco = new Endereco();
        endereco.setCep("12345678"); // Sem hífen

        Cliente cliente = new Cliente();
        cliente.setNome("Empresa Teste");
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEndereco(endereco);

        Set<String> mensagensErro = validator.validate(cliente).stream()
                .filter(v -> v.getPropertyPath().toString().equals("endereco.cep"))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(mensagensErro.contains("CEP deve estar no formato XXXXX-XXX"));
    }

    @Test
    public void quandoCriarEntidade_devePreencherDataCriacaoEAualizacao() {
        Cliente cliente = new Cliente();
        cliente.setNome("Empresa Teste");
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEndereco(new Endereco("Rua Teste", "12345-678", "São Paulo", "SP"));

        // Simula o salvamento (em um teste real, isso seria feito pelo repositório)
        cliente.setDataCriacao(LocalDateTime.now());
        cliente.setDataAtualizacao(LocalDateTime.now());

        assertNotNull(cliente.getDataCriacao());
        assertNotNull(cliente.getDataAtualizacao());
    }

    @Test
    public void quandoAtualizarEntidade_deveAtualizarDataAtualizacao() {
        Cliente cliente = new Cliente();
        cliente.setNome("Empresa Teste");
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEndereco(new Endereco("Rua Teste", "12345-678", "São Paulo", "SP"));

        LocalDateTime dataCriacao = LocalDateTime.now().minusDays(1);
        cliente.setDataCriacao(dataCriacao);
        cliente.setDataAtualizacao(dataCriacao);

        // Simula atualização
        cliente.setNome("Nome Atualizado");
        cliente.setDataAtualizacao(LocalDateTime.now());

        assertEquals("Nome Atualizado", cliente.getNome());
        assertEquals(dataCriacao, cliente.getDataCriacao()); // Data criação mantém
        assertTrue(cliente.getDataAtualizacao().isAfter(dataCriacao)); // Data atualização é mais recente
    }
}
