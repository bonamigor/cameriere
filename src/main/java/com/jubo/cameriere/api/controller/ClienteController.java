package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.ClienteAssembler;
import com.jubo.cameriere.api.dto.ClienteRequestDTO;
import com.jubo.cameriere.api.dto.ClienteResponseDTO;
import com.jubo.cameriere.domain.model.Cliente;
import com.jubo.cameriere.domain.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteAssembler clienteAssembler;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodosClientes() {
        List<ClienteResponseDTO> response = clienteService.listarTodosClientes().stream()
                .map(clienteAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteAssembler.toResponseDTO(clienteService.buscarClientePorId(id)));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(
            @RequestBody @Valid ClienteRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        Cliente criado = clienteService.criarCliente(clienteAssembler.toEntity(request));
        ClienteResponseDTO response = clienteAssembler.toResponseDTO(criado);
        URI uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid ClienteRequestDTO request) {
        Cliente atualizado = clienteService.atualizarCliente(id, clienteAssembler.toEntity(request));
        return ResponseEntity.ok(clienteAssembler.toResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        clienteService.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
