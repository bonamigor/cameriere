package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.ClienteNaoEncontradoException;
import com.jubo.cameriere.domain.exception.CnpjJaCadastradoException;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.repository.ClienteRepository;
import com.jubo.cameriere.domain.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public List<Cliente> listarTodosClientes() {
        return clienteRepository.findAllWithEmpresa();
    }

    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Cliente criarCliente(Cliente cliente) {
        if (clienteRepository.existsByCnpj(cliente.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
        }

        if (cliente.getAtivo() == null) {
            cliente.setAtivo(true);
        }
        aplicarEmpresa(cliente, cliente.getEmpresa());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizarCliente(Long id, Cliente dados) {
        Cliente cliente = buscarOuFalhar(id);

        if (!cliente.getCnpj().equals(dados.getCnpj()) && clienteRepository.existsByCnpj(dados.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
        }

        cliente.setNome(dados.getNome());
        cliente.setCnpj(dados.getCnpj());
        cliente.setEmail(dados.getEmail());
        cliente.setTelefone(dados.getTelefone());
        aplicarEmpresa(cliente, dados.getEmpresa());
        if (dados.getAtivo() != null) {
            cliente.setAtivo(dados.getAtivo());
        }
        if (dados.getEndereco() != null) {
            cliente.setEndereco(dados.getEndereco());
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deletarCliente(Long id) {
        Cliente cliente = buscarOuFalhar(id);
        clienteRepository.delete(cliente);
    }

    public Cliente buscarOuFalhar(Long codigoCliente) {
        return clienteRepository.findById(codigoCliente)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: " + codigoCliente));
    }

    private void aplicarEmpresa(Cliente cliente, Empresa empresa) {
        if (empresa == null || empresa.getId() == null) {
            cliente.setEmpresa(null);
            return;
        }

        Empresa vinculada = empresaRepository.findById(empresa.getId())
                .orElseThrow(() -> new com.jubo.cameriere.domain.exception.EmpresaNaoEncontradaException(
                        "Empresa não encontrada: " + empresa.getId()));
        cliente.setEmpresa(vinculada);
    }
}
