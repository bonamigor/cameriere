package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.*;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.model.EstanteProduto;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.repository.EstanteProdutoRepository;
import com.jubo.cameriere.domain.repository.EstanteRepository;
import com.jubo.cameriere.domain.repository.ProdutoRepository;
import com.jubo.cameriere.domain.service.comando.EstanteProdutoAssociateCommand;
import com.jubo.cameriere.domain.service.comando.EstanteProdutoUpdateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EstanteProdutoService {

    private final EstanteProdutoRepository estanteProdutoRepository;
    private final EstanteRepository estanteRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public EstanteProduto associarProdutoEstante(EstanteProdutoAssociateCommand comando) {
        log.info("Associando produto {} à estante {}", comando.produtoId(), comando.estanteId());

        Estante estante = estanteRepository.findById(comando.estanteId())
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + comando.estanteId()));

        if (!estante.getAtiva()) {
            throw new EstanteInativaException("Não é possível associar produtos a uma estante inativa");
        }

        Produto produto = produtoRepository.findById(comando.produtoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + comando.produtoId()));

        if (!produto.getAtivo()) {
            throw new ProdutoInativoException("Não é possível associar um produto inativo");
        }

        if (estanteProdutoRepository.existsByEstanteIdAndProdutoId(comando.estanteId(), comando.produtoId())) {
            throw new ProdutoJaAssociadoException(
                    String.format("Produto %d já está associado à estante %d",
                            comando.produtoId(), comando.estanteId()));
        }

        validarPrecoVendaNaoMenorQueCusto(comando.precoVenda(), produto.getPrecoCusto());

        EstanteProduto estanteProduto = EstanteProduto.builder()
                .estante(estante)
                .produto(produto)
                .precoVenda(comando.precoVenda())
                .quantidade(comando.quantidadeEstoque())
                .ativo(true)
                .build();

        EstanteProduto saved = estanteProdutoRepository.save(estanteProduto);
        return estanteProdutoRepository.findByIdWithAssociacoes(saved.getId()).orElse(saved);
    }

    @Transactional
    public EstanteProduto atualizarAssociacao(Long id, EstanteProdutoUpdateCommand comando) {
        log.info("Atualizando associação ID: {}", id);

        EstanteProduto estanteProduto = estanteProdutoRepository.findByIdWithAssociacoes(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));

        if (comando.precoVenda() != null) {
            validarPrecoVendaNaoMenorQueCusto(
                    comando.precoVenda(),
                    estanteProduto.getProduto().getPrecoCusto());
            estanteProduto.setPrecoVenda(comando.precoVenda());
        }

        if (comando.quantidadeEstoque() != null) {
            estanteProduto.setQuantidade(comando.quantidadeEstoque());
        }

        if (comando.ativo() != null) {
            estanteProduto.setAtivo(comando.ativo());
        }

        estanteProdutoRepository.save(estanteProduto);
        return estanteProdutoRepository.findByIdWithAssociacoes(id).orElse(estanteProduto);
    }

    @Transactional
    public EstanteProduto ajustarPreco(Long id, BigDecimal novoPreco, String motivo) {
        log.info("Ajustando preço do produto {} para R$ {}", id, novoPreco);

        EstanteProduto estanteProduto = estanteProdutoRepository.findByIdWithAssociacoes(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));

        Double precoCusto = estanteProduto.getProduto().getPrecoCusto();
        validarPrecoVendaNaoMenorQueCusto(novoPreco, precoCusto);

        BigDecimal margem = novoPreco.subtract(BigDecimal.valueOf(precoCusto))
                .divide(BigDecimal.valueOf(precoCusto), 2, RoundingMode.HALF_UP);

        if (margem.compareTo(new BigDecimal("0.05")) < 0) {
            throw new MargemLucroInsuficienteException(
                    String.format("Margem de lucro de %.2f%% é inferior ao mínimo de 5%%",
                            margem.multiply(new BigDecimal("100"))));
        }

        estanteProduto.setPrecoVenda(novoPreco);
        estanteProdutoRepository.save(estanteProduto);
        return estanteProdutoRepository.findByIdWithAssociacoes(id).orElse(estanteProduto);
    }

    public EstanteProduto buscarPorId(Long id) {
        return estanteProdutoRepository.findByIdWithAssociacoes(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));
    }

    public Page<EstanteProduto> listarPorEstante(Long estanteId, Pageable pageable) {
        return estanteProdutoRepository.findByEstanteIdAndAtivoTrue(estanteId, pageable);
    }

    public List<EstanteProduto> buscarProdutosDisponiveis(Long estanteId) {
        return estanteProdutoRepository.findProdutosDisponiveisNaEstante(estanteId);
    }

    private void validarPrecoVendaNaoMenorQueCusto(BigDecimal precoVenda, Double precoCusto) {
        if (precoVenda.compareTo(BigDecimal.valueOf(precoCusto)) < 0) {
            throw new PrecoVendaMenorQueCustoException(
                    String.format("Preço de venda (R$ %.2f) não pode ser menor que o custo (R$ %.2f)",
                            precoVenda, precoCusto));
        }
    }
}
