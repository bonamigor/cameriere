package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.ClienteNaoEncontradoException;
import com.jubo.cameriere.api.exceptions.CnpjJaCadastradoException;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodosClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Cliente criarCliente(Cliente cliente) {
        if (clienteRepository.existsByCnpj(cliente.getCnpj())) {
            throw new CnpjJaCadastradoException("CNPJ já cadastrado");
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizarCliente(Cliente cliente) {
        Cliente clienteRecuperado = buscarOuFalhar(cliente.getId());

        clienteRecuperado.setNome(cliente.getNome());
        clienteRecuperado.setCnpj(cliente.getCnpj());
        clienteRecuperado.setEndereco(cliente.getEndereco());
        clienteRecuperado.setEmail(cliente.getEmail());
        clienteRecuperado.setTelefone(cliente.getTelefone());
        clienteRecuperado.setAtivo(cliente.getAtivo());
        clienteRecuperado.setUsuario(cliente.getUsuario());

        return clienteRepository.save(clienteRecuperado);
    }

    @Transactional
    public void deletarCliente(Long id) {
        clienteRepository.deleteById(id);
    }

    public Cliente buscarOuFalhar(Long codigoCliente) {
        return clienteRepository.findById(codigoCliente)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
    }
}