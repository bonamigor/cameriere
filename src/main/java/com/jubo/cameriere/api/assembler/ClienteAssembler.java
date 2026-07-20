package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.ClienteRequestDTO;
import com.jubo.cameriere.api.dto.ClienteResponseDTO;
import com.jubo.cameriere.api.dto.EnderecoDTO;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class ClienteAssembler {

    public Cliente toEntity(ClienteRequestDTO request) {
        Cliente cliente = new Cliente();
        copyToEntity(request, cliente);
        return cliente;
    }

    public void copyToEntity(ClienteRequestDTO request, Cliente cliente) {
        cliente.setNome(request.nome());
        cliente.setCnpj(request.cnpj());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        if (request.empresaId() != null) {
            Empresa empresa = new Empresa();
            empresa.setId(request.empresaId());
            cliente.setEmpresa(empresa);
        }
        else {
            cliente.setEmpresa(null);
        }
        if (request.ativo() != null) {
            cliente.setAtivo(request.ativo());
        }
        if (request.endereco() != null) {
            cliente.setEndereco(toEndereco(request.endereco()));
        }
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCnpj(),
                toEnderecoDTO(cliente.getEndereco()),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null,
                cliente.getEmpresa() != null ? cliente.getEmpresa().getNome() : null,
                cliente.getAtivo(),
                cliente.getDataCriacao(),
                cliente.getDataAtualizacao()
        );
    }

    private Endereco toEndereco(EnderecoDTO dto) {
        return new Endereco(
                dto.logradouro(),
                dto.numero(),
                dto.bairro(),
                dto.cep(),
                dto.cidade(),
                dto.estado()
        );
    }

    private EnderecoDTO toEnderecoDTO(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoDTO(
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCep(),
                endereco.getCidade(),
                endereco.getEstado()
        );
    }
}
