package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.CnpjJaCadastradoException;
import com.jubo.cameriere.domain.exception.EmpresaNaoEncontradaException;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<Empresa> listarTodasEmpresas() {
        return empresaRepository.findAll();
    }

    public Empresa buscarEmpresaPorId(Long id) {
        return buscarOuFalhar(id);
    }

    @Transactional
    public Empresa criarEmpresa(Empresa empresa) {
        if (empresaRepository.existsByCnpj(empresa.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
        }
        if (empresa.getAtivo() == null) {
            empresa.setAtivo(true);
        }
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa atualizarEmpresa(Long id, Empresa dados) {
        Empresa empresa = buscarOuFalhar(id);

        if (!empresa.getCnpj().equals(dados.getCnpj()) && empresaRepository.existsByCnpj(dados.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
        }

        empresa.setNome(dados.getNome());
        empresa.setCnpj(dados.getCnpj());
        empresa.setEmail(dados.getEmail());
        empresa.setTelefone(dados.getTelefone());
        empresa.setEndereco(dados.getEndereco());
        if (dados.getAtivo() != null) {
            empresa.setAtivo(dados.getAtivo());
        }

        return empresaRepository.save(empresa);
    }

    @Transactional
    public void deletarEmpresa(Long id) {
        Empresa empresa = buscarOuFalhar(id);
        empresaRepository.delete(empresa);
    }

    public Empresa buscarOuFalhar(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada: " + id));
    }
}
