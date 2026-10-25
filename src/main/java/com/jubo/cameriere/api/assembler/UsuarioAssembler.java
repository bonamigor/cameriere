package com.jubo.cameriere.api.assembler;

import com.jubo.cameriere.api.dto.UsuarioRequestDTO;
import com.jubo.cameriere.api.dto.UsuarioResponseDTO;
import com.jubo.cameriere.api.dto.UsuarioUpdateDTO;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.service.comando.UsuarioCreateCommand;
import com.jubo.cameriere.domain.service.comando.UsuarioUpdateCommand;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAssembler {

    public UsuarioCreateCommand toCreateCommand(UsuarioRequestDTO request) {
        return new UsuarioCreateCommand(
                request.nome(),
                request.email(),
                request.senha(),
                request.admin(),
                request.clienteId()
        );
    }

    public UsuarioUpdateCommand toUpdateCommand(UsuarioUpdateDTO request) {
        return new UsuarioUpdateCommand(
                request.nome(),
                request.email(),
                request.senha(),
                request.admin(),
                request.clienteId()
        );
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        Cliente cliente = usuario.getCliente();
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getAdmin(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                usuario.isTotpAtivo(),
                usuario.getDataCriacao(),
                usuario.getDataAtualizacao()
        );
    }
}
