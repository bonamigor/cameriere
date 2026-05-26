package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.UsuarioNaoEncontradoException;
import com.jubo.cameriere.api.exceptions.EmailJaCadastradoException;
import com.jubo.cameriere.api.exceptions.ClienteJaPossuiUsuarioException;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorClienteId(Long clienteId) {
        return usuarioRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        // Verifica se email já existe
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }

        // Verifica se cliente já possui usuário
        if (usuario.getCliente() != null && usuario.getCliente().getId() != null) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByClienteId(usuario.getCliente().getId());
            if (usuarioExistente.isPresent()) {
                throw new ClienteJaPossuiUsuarioException("Cliente já possui um usuário cadastrado");
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new UsuarioNaoEncontradoException("Usuário não encontrado");
        }

        // Verifica se email já existe em outro usuário
        Optional<Usuario> usuarioComEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioComEmail.isPresent() && !usuarioComEmail.get().getId().equals(usuario.getId())) {
            throw new EmailJaCadastradoException("Email já cadastrado em outro usuário");
        }

        // Verifica se cliente já possui usuário em outro registro
        if (usuario.getCliente() != null && usuario.getCliente().getId() != null) {
            Optional<Usuario> usuarioComCliente = usuarioRepository.findByClienteId(usuario.getCliente().getId());
            if (usuarioComCliente.isPresent() && !usuarioComCliente.get().getId().equals(usuario.getId())) {
                throw new ClienteJaPossuiUsuarioException("Cliente já possui um usuário cadastrado");
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}