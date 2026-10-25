package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.UsuarioAssembler;
import com.jubo.cameriere.api.dto.UsuarioRequestDTO;
import com.jubo.cameriere.api.dto.UsuarioResponseDTO;
import com.jubo.cameriere.api.dto.UsuarioUpdateDTO;
import com.jubo.cameriere.api.security.MfaService;
import com.jubo.cameriere.api.security.UsuarioAutenticado;
import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.service.AuditoriaService;
import com.jubo.cameriere.domain.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAssembler usuarioAssembler;
    private final MfaService mfaService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodosUsuarios() {
        List<UsuarioResponseDTO> response = usuarioService.listarTodosUsuarios().stream()
                .map(usuarioAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioAssembler.toResponseDTO(usuarioService.buscarUsuarioPorId(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(usuarioAssembler.toResponseDTO(usuarioService.buscarUsuarioPorClienteId(clienteId)));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(
            @RequestBody @Valid UsuarioRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        Usuario criado = usuarioService.criarUsuario(usuarioAssembler.toCreateCommand(request));
        UsuarioResponseDTO response = usuarioAssembler.toResponseDTO(criado);
        URI uri = uriBuilder.path("/api/usuarios/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioUpdateDTO request) {
        Usuario atualizado = usuarioService.atualizarUsuario(id, usuarioAssembler.toUpdateCommand(request));
        return ResponseEntity.ok(usuarioAssembler.toResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /** Para quem perdeu o celular e os códigos de recuperação: o usuário configura o 2FA de novo. */
    @DeleteMapping("/{id}/2fa")
    public ResponseEntity<Void> redefinirMfa(@PathVariable Long id,
                                             @AuthenticationPrincipal UsuarioAutenticado admin,
                                             HttpServletRequest http) {
        mfaService.redefinir(id);
        auditoriaService.registrar(AcaoAuditoria.MFA_REDEFINIDO, admin.getId(), admin.getEmail(),
                http.getRemoteAddr(), "Usuário " + id);
        return ResponseEntity.noContent().build();
    }
}
