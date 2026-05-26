package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.UnidadeMedidaNaoEncontradaException;
import com.jubo.cameriere.api.exceptions.SiglaJaCadastradaException;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import com.jubo.cameriere.domain.repository.UnidadeMedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public UnidadeMedidaService(UnidadeMedidaRepository unidadeMedidaRepository) {
        this.unidadeMedidaRepository = unidadeMedidaRepository;
    }

    @Transactional(readOnly = true)
    public List<UnidadeMedida> listarTodasUnidadesMedida() {
        return unidadeMedidaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UnidadeMedida> buscarUnidadeMedidaPorId(Long id) {
        return unidadeMedidaRepository.findById(id);
    }

    @Transactional
    public UnidadeMedida criarUnidadeMedida(UnidadeMedida unidadeMedida) {
        if (unidadeMedidaRepository.existsBySigla(unidadeMedida.getSigla())) {
            throw new SiglaJaCadastradaException("Sigla já cadastrada");
        }
        return unidadeMedidaRepository.save(unidadeMedida);
    }

    @Transactional
    public UnidadeMedida atualizarUnidadeMedida(UnidadeMedida unidadeMedida) {
        if (!unidadeMedidaRepository.existsById(unidadeMedida.getId())) {
            throw new UnidadeMedidaNaoEncontradaException("Unidade de medida não encontrada");
        }

        Optional<UnidadeMedida> unidadeExistente = unidadeMedidaRepository.findBySigla(unidadeMedida.getSigla());
        if (unidadeExistente.isPresent() && !unidadeExistente.get().getId().equals(unidadeMedida.getId())) {
            throw new SiglaJaCadastradaException("Sigla já cadastrada em outra unidade de medida");
        }

        return unidadeMedidaRepository.save(unidadeMedida);
    }

    @Transactional
    public void deletarUnidadeMedida(Long id) {
        unidadeMedidaRepository.deleteById(id);
    }
}