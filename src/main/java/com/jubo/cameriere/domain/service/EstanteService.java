package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.*;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.repository.ClienteRepository;
import com.jubo.cameriere.domain.repository.EstanteRepository;
import com.jubo.cameriere.domain.service.comando.EstanteCreateCommand;
import com.jubo.cameriere.domain.service.comando.EstanteUpdateCommand;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Estante criarEstante(EstanteCreateCommand comando) {
        log.info("Criando estante para cliente: {} no período: {}", comando.clienteId(), comando.periodo());

        Cliente cliente = clienteRepository.findById(comando.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: " + comando.clienteId()));

        if (!cliente.getAtivo()) {
            throw new ClienteInativoException("Não é possível criar estante para cliente inativo");
        }

        validarPeriodo(comando.periodo());

        if (estanteRepository.existsByPeriodoAndClienteIdAndAtivaTrue(comando.periodo(), comando.clienteId())) {
            throw new EstanteJaExisteException(
                    String.format("Já existe uma estante ativa para o cliente %d no período %s",
                            comando.clienteId(), comando.periodo()));
        }

        Estante estante = Estante.builder()
                .periodo(comando.periodo())
                .observacao(comando.observacao())
                .cliente(cliente)
                .ativa(comando.ativa() != null ? comando.ativa() : true)
                .empresa(comando.empresa())
                .build();

        Estante saved = estanteRepository.saveAndFlush(estante);
        return carregarCompleta(saved.getId());
    }

    @Transactional
    public Estante atualizarEstante(Long id, EstanteUpdateCommand comando) {
        log.info("Atualizando estante: {}", id);

        Estante estante = carregarCompleta(id);

        if (comando.periodo() != null) {
            validarPeriodo(comando.periodo());

            if (estanteRepository.existsByPeriodoAndClienteIdAndAtivaTrue(
                    comando.periodo(), estante.getCliente().getId()) &&
                    !estante.getPeriodo().equals(comando.periodo())) {
                throw new EstanteJaExisteException(
                        String.format("Já existe estante ativa no período %s para este cliente", comando.periodo()));
            }
            estante.setPeriodo(comando.periodo());
        }

        if (comando.observacao() != null) {
            estante.setObservacao(comando.observacao());
        }

        if (comando.ativa() != null) {
            if (!comando.ativa() && temProdutosAssociados(estante)) {
                throw new EstanteComProdutosException(
                        "Não é possível desativar estante que possui produtos associados");
            }
            estante.setAtiva(comando.ativa());
        }

        if (comando.empresa() != null) {
            estante.setEmpresa(comando.empresa());
        }

        estanteRepository.save(estante);
        return carregarCompleta(id);
    }

    @Transactional
    public void desativarEstante(Long id) {
        log.info("Desativando estante: {}", id);

        Estante estante = carregarCompleta(id);

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

        Estante estante = carregarCompleta(id);

        if (temProdutosAssociados(estante)) {
            throw new EstanteComProdutosException(
                    "Não é possível deletar estante que possui produtos associados");
        }

        estanteRepository.delete(estante);
    }

    public Estante buscarEstantePorId(Long id) {
        return carregarCompleta(id);
    }

    public Page<Estante> listarEstantesPorCliente(Long clienteId, Pageable pageable) {
        return estanteRepository.findByClienteIdAndAtivaTrue(clienteId, pageable);
    }

    public List<Estante> listarEstantesAtivasPorPeriodo(String periodo) {
        validarPeriodo(periodo);
        return estanteRepository.findEstantesAtivasPorPeriodo(periodo);
    }

    private Estante carregarCompleta(Long id) {
        return estanteRepository.findByIdWithProdutos(id)
                .orElseThrow(() -> new EntityNotFoundException("Estante não encontrada: " + id));
    }

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
}
