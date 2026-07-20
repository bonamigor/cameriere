package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.EmpresaRequestDTO;
import com.jubo.cameriere.api.dto.EmpresaResponseDTO;
import com.jubo.cameriere.api.dto.EnderecoDTO;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EmpresaAssembler {

    public Empresa toEntity(EmpresaRequestDTO request) {
        Empresa empresa = new Empresa();
        copyToEntity(request, empresa);
        return empresa;
    }

    public void copyToEntity(EmpresaRequestDTO request, Empresa empresa) {
        empresa.setNome(request.nome());
        empresa.setCnpj(request.cnpj());
        empresa.setEmail(request.email());
        empresa.setTelefone(request.telefone());
        if (request.ativo() != null) {
            empresa.setAtivo(request.ativo());
        }
        empresa.setEndereco(request.endereco() != null ? toEndereco(request.endereco()) : null);
    }

    public EmpresaResponseDTO toResponseDTO(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getCnpj(),
                toEnderecoDTO(empresa.getEndereco()),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.getAtivo(),
                empresa.getDataCriacao(),
                empresa.getDataAtualizacao()
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
