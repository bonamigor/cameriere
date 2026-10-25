package com.jubo.cameriere.config;

import com.jubo.cameriere.domain.model.*;
import com.jubo.cameriere.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "cameriere.seed.enabled", havingValue = "true")
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final ProdutoRepository produtoRepository;
    private final EstanteRepository estanteRepository;
    private final EstanteProdutoRepository estanteProdutoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            log.info("Seed ignorado: base já possui dados");
            return;
        }

        UnidadeMedida kg = new UnidadeMedida(null, "KG", "Quilograma");
        UnidadeMedida un = new UnidadeMedida(null, "UN", "Unidade");
        UnidadeMedida l = new UnidadeMedida(null, "L", "Litro");
        unidadeMedidaRepository.saveAll(java.util.List.of(kg, un, l));

        Produto cafe = new Produto(null, "Café Especial", 25.50, kg, true, null);
        Produto acucar = new Produto(null, "Açúcar Cristal", 3.20, kg, true, null);
        Produto copo = new Produto(null, "Copo Descartável 200ml", 0.15, un, true, null);
        produtoRepository.saveAll(java.util.List.of(cafe, acucar, copo));

        Endereco endereco = new Endereco("Rua das Empresas", "100", "Centro", "01234-567", "São Paulo", "SP");
        Cliente cliente = new Cliente();
        cliente.setNome("Cafeteria Central");
        cliente.setCnpj("12.345.678/0001-90");
        cliente.setEndereco(endereco);
        cliente.setEmail("contato@cafeteriacentral.com");
        cliente.setTelefone("(11) 99999-8888");
        cliente.setAtivo(true);
        cliente = clienteRepository.save(cliente);

        Usuario usuario = new Usuario(null, "Admin Central", "admin@cafeteriacentral.com",
                passwordEncoder.encode("123456"), true, null);
        usuarioRepository.save(usuario);

        Usuario operador = new Usuario(null, "Operador Central", "operador@cafeteriacentral.com",
                passwordEncoder.encode("123456"), false, cliente);
        usuarioRepository.save(operador);

        Estante estante = Estante.builder()
                .periodo("2026/01")
                .observacao("Estante de produtos para Janeiro/2026")
                .ativa(true)
                .empresa(1)
                .cliente(cliente)
                .build();
        estante = estanteRepository.save(estante);

        EstanteProduto epCafe = EstanteProduto.builder()
                .estante(estante)
                .produto(cafe)
                .precoVenda(new BigDecimal("42.90"))
                .quantidade(BigDecimal.valueOf(100))
                .ativo(true)
                .build();

        EstanteProduto epAcucar = EstanteProduto.builder()
                .estante(estante)
                .produto(acucar)
                .precoVenda(new BigDecimal("5.50"))
                .quantidade(BigDecimal.valueOf(200))
                .ativo(true)
                .build();

        EstanteProduto epCopo = EstanteProduto.builder()
                .estante(estante)
                .produto(copo)
                .precoVenda(new BigDecimal("0.25"))
                .quantidade(BigDecimal.valueOf(5000))
                .ativo(true)
                .build();

        estanteProdutoRepository.saveAll(java.util.List.of(epCafe, epAcucar, epCopo));

        log.info("===== DADOS DE SEED CARREGADOS =====");
        log.info("Cliente ID={}", cliente.getId());
        log.info("Admin: admin@cafeteriacentral.com / 123456 (ROLE_ADMIN, sem cliente)");
        log.info("User:  operador@cafeteriacentral.com / 123456 (ROLE_USER, cliente={})", cliente.getId());
        log.info("Estante ID={} período={}", estante.getId(), estante.getPeriodo());
        log.info("Produtos na estante: Café={}, Açúcar={}, Copo={}", cafe.getId(), acucar.getId(), copo.getId());
    }
}
