package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.ClienteJaPossuiUsuarioException;
import com.jubo.cameriere.domain.exception.EmailJaCadastradoException;
import com.jubo.cameriere.domain.exception.NegocioException;
import com.jubo.cameriere.domain.exception.UsuarioNaoEncontradoException;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.repository.UsuarioRepository;
import com.jubo.cameriere.domain.service.comando.UsuarioCreateCommand;
import com.jubo.cameriere.domain.service.comando.UsuarioUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAllWithCliente();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return buscarOuFalhar(id);
    }

    public Usuario buscarUsuarioPorClienteId(Long clienteId) {
        return usuarioRepository.findAllByClienteIdWithCliente(clienteId).stream()
                .findFirst()
                .orElseThrow(() -> new UsuarioNaoEncontradoException(
                        "Usuário não encontrado para o cliente: " + clienteId));
    }

    @Transactional
    public Usuario criarUsuario(UsuarioCreateCommand comando) {
        if (usuarioRepository.existsByEmail(comando.email())) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }

        validarRelacaoAdminCliente(Boolean.TRUE.equals(comando.admin()), comando.clienteId());

        Usuario usuario = new Usuario();
        usuario.setNome(comando.nome());
        usuario.setEmail(comando.email());
        usuario.setSenha(passwordEncoder.encode(comando.senha()));
        usuario.setAdmin(comando.admin());
        usuario.setCliente(resolverCliente(comando.clienteId(), null));

        Usuario saved = usuarioRepository.save(usuario);
        return buscarOuFalhar(saved.getId());
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, UsuarioUpdateCommand comando) {
        Usuario usuario = buscarOuFalhar(id);

        usuarioRepository.findByEmail(comando.email())
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new EmailJaCadastradoException("Email já cadastrado em outro usuário");
                });

        validarRelacaoAdminCliente(Boolean.TRUE.equals(comando.admin()), comando.clienteId());

        usuario.setNome(comando.nome());
        usuario.setEmail(comando.email());
        usuario.setAdmin(comando.admin());
        if (comando.senha() != null && !comando.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(comando.senha()));
        }
        usuario.setCliente(resolverCliente(comando.clienteId(), id));

        usuarioRepository.save(usuario);
        return buscarOuFalhar(id);
    }

    private void validarRelacaoAdminCliente(boolean admin, Long clienteId) {
        if (admin && clienteId != null) {
            throw new NegocioException(
                    "Usuários administradores não podem estar associados a um cliente");
        }
        if (!admin && clienteId == null) {
            throw new NegocioException(
                    "Operadores devem estar associados a um cliente");
        }
    }

    @Transactional
    public void deletarUsuario(Long id) {
        Usuario usuario = buscarOuFalhar(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarOuFalhar(Long id) {
        return usuarioRepository.findByIdWithCliente(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private Cliente resolverCliente(Long clienteId, Long usuarioIdAtual) {
        if (clienteId == null) {
            return null;
        }

        Cliente cliente = clienteService.buscarOuFalhar(clienteId);
        boolean clienteJaPossuiUsuario = usuarioIdAtual == null
                ? usuarioRepository.existsByClienteId(clienteId)
                : usuarioRepository.existsByClienteIdAndIdNot(clienteId, usuarioIdAtual);

        if (clienteJaPossuiUsuario) {
            throw new ClienteJaPossuiUsuarioException("Cliente já possui um usuário cadastrado");
        }
        return cliente;
    }
}
