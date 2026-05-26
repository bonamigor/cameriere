package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.*;
import com.jubo.cameriere.domain.dto.EstanteProdutoRequestDTO;
import com.jubo.cameriere.domain.dto.EstanteProdutoResponseDTO;
import com.jubo.cameriere.domain.dto.EstanteProdutoUpdateDTO;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.model.EstanteProduto;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.repository.EstanteProdutoRepository;
import com.jubo.cameriere.domain.repository.EstanteRepository;
import com.jubo.cameriere.domain.repository.ProdutoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    // CRUD Principal
    @Transactional
    public EstanteProdutoResponseDTO associarProdutoEstante(@Valid EstanteProdutoRequestDTO request) {
        log.info("Associando produto {} à estante {}", request.getProdutoId(), request.getEstanteId());

        // Validações
        Estante estante = estanteRepository.findById(request.getEstanteId())
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + request.getEstanteId()));

        if (!estante.getAtiva()) {
            throw new EstanteInativaException("Não é possível associar produtos a uma estante inativa");
        }

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + request.getProdutoId()));

        if (!produto.getAtivo()) {
            throw new ProdutoInativoException("Não é possível associar um produto inativo");
        }

        // Verifica duplicidade
        if (estanteProdutoRepository.existsByEstanteIdAndProdutoId(request.getEstanteId(), request.getProdutoId())) {
            throw new ProdutoJaAssociadoException(
                    String.format("Produto %d já está associado à estante %d",
                            request.getProdutoId(), request.getEstanteId()));
        }

        // Valida preço
        if (request.getPrecoVenda().compareTo(BigDecimal.valueOf(produto.getPrecoCusto())) < 0) {
            throw new PrecoVendaMenorQueCustoException(
                    String.format("Preço de venda (R$ %.2f) não pode ser menor que o custo (R$ %.2f)",
                            request.getPrecoVenda(), produto.getPrecoCusto()));
        }

        // Cria associação
        EstanteProduto estanteProduto = EstanteProduto.builder()
                .estante(estante)
                .produto(produto)
                .precoVenda(request.getPrecoVenda())
                .ativo(true)
                .build();

        EstanteProduto saved = estanteProdutoRepository.save(estanteProduto);

        return toResponseDTO(saved);
    }

    @Transactional
    public EstanteProdutoResponseDTO atualizarAssociacao(Long id, EstanteProdutoUpdateDTO updateDTO) {
        log.info("Atualizando associação ID: {}", id);

        EstanteProduto estanteProduto = estanteProdutoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));

        if (updateDTO.getPrecoVenda() != null) {
            estanteProduto.setPrecoVenda(updateDTO.getPrecoVenda());
        }

        if (updateDTO.getAtivo() != null) {
            estanteProduto.setAtivo(updateDTO.getAtivo());
        }

        EstanteProduto updated = estanteProdutoRepository.save(estanteProduto);

        return toResponseDTO(updated);
    }

    @Transactional
    public EstanteProdutoResponseDTO ajustarPreco(Long id, BigDecimal novoPreco, String motivo) {
        log.info("Ajustando preço do produto {} para R$ {}", id, novoPreco);

        EstanteProduto estanteProduto = estanteProdutoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));

        BigDecimal precoAntigo = estanteProduto.getPrecoVenda();

        // Validação de margem
        BigDecimal margem = novoPreco.subtract(BigDecimal.valueOf(estanteProduto.getProduto().getPrecoCusto()))
                .divide(BigDecimal.valueOf(estanteProduto.getProduto().getPrecoCusto()), 2, RoundingMode.HALF_UP);

        if (margem.compareTo(new BigDecimal("0.05")) < 0) {
            throw new MargemLucroInsuficienteException(
                    String.format("Margem de lucro de %.2f%% é inferior ao mínimo de 5%%",
                            margem.multiply(new BigDecimal("100"))));
        }

        estanteProduto.setPrecoVenda(novoPreco);
        estanteProdutoRepository.save(estanteProduto);

        return toResponseDTO(estanteProduto);
    }

    public EstanteProdutoResponseDTO buscarPorId(Long id) {
        EstanteProduto estanteProduto = estanteProdutoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Associação não encontrada: " + id));
        return toResponseDTO(estanteProduto);
    }

    public Page<EstanteProdutoResponseDTO> listarPorEstante(Long estanteId, Pageable pageable) {
        return estanteProdutoRepository.findByEstanteIdAndAtivoTrue(estanteId, pageable)
                .map(this::toResponseDTO);
    }

    public List<EstanteProdutoResponseDTO> buscarProdutosDisponiveis(Long estanteId) {
        return estanteProdutoRepository.findProdutosDisponiveisNaEstante(estanteId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private EstanteProdutoResponseDTO toResponseDTO(EstanteProduto ep) {
        return EstanteProdutoResponseDTO.builder()
                .id(ep.getId())
                .estanteId(ep.getEstante().getId())
                .estantePeriodo(ep.getEstante().getPeriodo())
                .produtoId(ep.getProduto().getId())
                .produtoNome(ep.getProduto().getNome())
                .precoVenda(ep.getPrecoVenda())
                .ativo(ep.getAtivo())
                .dataCadastro(ep.getDataCadastro())
                .dataAtualizacao(ep.getDataAtualizacao())
                .disponivel(ep.isDisponivel())
                .build();
    }
}
