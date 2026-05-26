package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.*;
import com.jubo.cameriere.domain.dto.EstanteRequestDTO;
import com.jubo.cameriere.domain.dto.EstanteResponseDTO;
import com.jubo.cameriere.domain.dto.EstanteUpdateDTO;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.repository.ClienteRepository;
import com.jubo.cameriere.domain.repository.EstanteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EstanteService {

    private final EstanteRepository estanteRepository;
    private final ClienteRepository clienteRepository;

    private static final DateTimeFormatter PERIODO_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    // CRUD Principal
    @Transactional
    public EstanteResponseDTO criarEstante(EstanteRequestDTO request) {
        log.info("Criando estante para cliente: {} no período: {}", request.getClienteId(), request.getPeriodo());

        // Validar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: " + request.getClienteId()));

        if (!cliente.getAtivo()) {
            throw new ClienteInativoException("Não é possível criar estante para cliente inativo");
        }

        // Validar período
        validarPeriodo(request.getPeriodo());

        // Verificar se já existe estante ativa no período
        if (estanteRepository.existsByPeriodoAndClienteIdAndAtivaTrue(request.getPeriodo(), request.getClienteId())) {
            throw new EstanteJaExisteException(
                    String.format("Já existe uma estante ativa para o cliente %d no período %s",
                            request.getClienteId(), request.getPeriodo()));
        }

        // Criar estante
        Estante estante = Estante.builder()
                .periodo(request.getPeriodo())
                .observacao(request.getObservacao())
                .cliente(cliente)
                .ativa(request.getAtiva() != null ? request.getAtiva() : true)
                .empresa(request.getEmpresa())
                .build();

        Estante savedEstante = estanteRepository.save(estante);

        return toResponseDTO(savedEstante);
    }

    @Transactional
    public EstanteResponseDTO atualizarEstante(Long id, @Valid EstanteUpdateDTO updateDTO) {
        log.info("Atualizando estante: {}", id);

        Estante estante = estanteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + id));

        if (updateDTO.getPeriodo() != null) {
            validarPeriodo(updateDTO.getPeriodo());

            // Verificar se novo período não conflita com outra estante ativa do mesmo cliente
            if (estanteRepository.existsByPeriodoAndClienteIdAndAtivaTrue(
                    updateDTO.getPeriodo(), estante.getCliente().getId()) &&
                    !estante.getPeriodo().equals(updateDTO.getPeriodo())) {
                throw new EstanteJaExisteException(
                        String.format("Já existe estante ativa no período %s para este cliente", updateDTO.getPeriodo()));
            }
            estante.setPeriodo(updateDTO.getPeriodo());
        }

        if (updateDTO.getObservacao() != null) {
            estante.setObservacao(updateDTO.getObservacao());
        }

        if (updateDTO.getAtiva() != null) {
            if (!updateDTO.getAtiva() && temProdutosAssociados(estante)) {
                throw new EstanteComProdutosException(
                        "Não é possível desativar estante que possui produtos associados");
            }
            estante.setAtiva(updateDTO.getAtiva());
        }

        if (updateDTO.getEmpresa() != null) {
            estante.setEmpresa(updateDTO.getEmpresa());
        }

        Estante updatedEstante = estanteRepository.save(estante);


        return this.toResponseDTO(updatedEstante);
    }

    @Transactional
    public void desativarEstante(Long id) {
        log.info("Desativando estante: {}", id);

        Estante estante = estanteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + id));

        if (temProdutosAssociados(estante)) {
            throw new EstanteComProdutosException(
                    "Não é possível desativar estante que possui produtos associados. Remova os produtos primeiro.");
        }

        estante.setAtiva(false);
        estanteRepository.save(estante);
    }

    @Transactional
    public void deletarEstante(Long id) {
        log.info("Deletando estante: {}", id);

        Estante estante = estanteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + id));

        if (temProdutosAssociados(estante)) {
            throw new EstanteComProdutosException(
                    "Não é possível deletar estante que possui produtos associados");
        }

        estanteRepository.delete(estante);
    }

    // Consultas
    public EstanteResponseDTO buscarEstantePorId(Long id) {
        Estante estante = estanteRepository.findByIdWithProdutos(id)
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + id));
        return this.toResponseDTO(estante);
    }

    public Page<EstanteResponseDTO> listarEstantesPorCliente(Long clienteId, Pageable pageable) {
        return estanteRepository.findByClienteIdAndAtivaTrue(clienteId, pageable)
                .map(this::toResponseDTO);
    }

    public List<EstanteResponseDTO> listarEstantesAtivasPorPeriodo(String periodo) {
        validarPeriodo(periodo);
        return estanteRepository.findEstantesAtivasPorPeriodo(periodo)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // Métodos utilitários
    private void validarPeriodo(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}/\\d{2}")) {
            throw new PeriodoInvalidoException(
                    "Período inválido. Formato esperado: YYYY/MM (ex: 2024/01)");
        }

        try {
            YearMonth.parse(periodo, PERIODO_FORMATTER);
        } catch (Exception e) {
            throw new PeriodoInvalidoException("Data do período inválida: " + periodo);
        }
    }

    private boolean temProdutosAssociados(Estante estante) {
        return estante.getProdutos() != null && !estante.getProdutos().isEmpty();
    }

    private EstanteResponseDTO toResponseDTO(Estante estante) {
        if (estante == null) return null;

        int quantidadeProdutos = estante.getProdutos() != null ? estante.getProdutos().size() : 0;

        return EstanteResponseDTO.builder()
                .id(estante.getId())
                .periodo(estante.getPeriodo())
                .observacao(estante.getObservacao())
                .clienteId(estante.getCliente().getId())
                .clienteNome(estante.getCliente().getNome())
                .clienteCnpj(estante.getCliente().getCnpj())
                .ativa(estante.getAtiva())
                .empresa(estante.getEmpresa())
                .quantidadeProdutos(quantidadeProdutos)
                .build();
    }
}
