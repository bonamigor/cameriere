package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.SiglaJaCadastradaException;
import com.jubo.cameriere.domain.exception.UnidadeMedidaNaoEncontradaException;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import com.jubo.cameriere.domain.repository.UnidadeMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository unidadeMedidaRepository;

    public List<UnidadeMedida> listarTodasUnidadesMedida() {
        return unidadeMedidaRepository.findAll();
    }

    public UnidadeMedida buscarUnidadeMedidaPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional
    public UnidadeMedida criarUnidadeMedida(UnidadeMedida unidade) {
        if (unidadeMedidaRepository.existsBySigla(unidade.getSigla())) {
            throw new SiglaJaCadastradaException("Sigla já cadastrada");
        }
        return unidadeMedidaRepository.save(unidade);
    }

    @Transactional
    public UnidadeMedida atualizarUnidadeMedida(Long id, UnidadeMedida dados) {
        UnidadeMedida unidade = buscarOuFalhar(id);

        unidadeMedidaRepository.findBySigla(dados.getSigla())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new SiglaJaCadastradaException("Sigla já cadastrada em outra unidade de medida");
                });

        unidade.setSigla(dados.getSigla());
        unidade.setDescricao(dados.getDescricao());
        return unidadeMedidaRepository.save(unidade);
    }

    @Transactional
    public void deletarUnidadeMedida(Long id) {
        UnidadeMedida unidade = buscarOuFalhar(id);
        unidadeMedidaRepository.delete(unidade);
    }

    private UnidadeMedida buscarOuFalhar(Long id) {
        return unidadeMedidaRepository.findById(id)
                .orElseThrow(() -> new UnidadeMedidaNaoEncontradaException(
                        "Unidade de medida não encontrada: " + id));
    }
}
