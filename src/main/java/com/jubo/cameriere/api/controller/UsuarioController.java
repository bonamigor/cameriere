package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.UsuarioAssembler;
import com.jubo.cameriere.api.dto.UsuarioRequestDTO;
import com.jubo.cameriere.api.dto.UsuarioResponseDTO;
import com.jubo.cameriere.api.dto.UsuarioUpdateDTO;
import com.jubo.cameriere.domain.model.Usuario;
import com.jubo.cameriere.domain.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
