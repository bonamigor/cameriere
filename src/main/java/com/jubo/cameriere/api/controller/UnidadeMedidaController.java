package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.UnidadeMedidaAssembler;
import com.jubo.cameriere.api.dto.UnidadeMedidaRequestDTO;
import com.jubo.cameriere.api.dto.UnidadeMedidaResponseDTO;
import com.jubo.cameriere.domain.model.UnidadeMedida;
import com.jubo.cameriere.domain.service.UnidadeMedidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadeMedidaController {

    private final UnidadeMedidaService unidadeMedidaService;
    private final UnidadeMedidaAssembler unidadeMedidaAssembler;

    @GetMapping
    public ResponseEntity<List<UnidadeMedidaResponseDTO>> listarTodasUnidadesMedida() {
        List<UnidadeMedidaResponseDTO> response = unidadeMedidaService.listarTodasUnidadesMedida().stream()
                .map(unidadeMedidaAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeMedidaResponseDTO> buscarUnidadeMedidaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                unidadeMedidaAssembler.toResponseDTO(unidadeMedidaService.buscarUnidadeMedidaPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UnidadeMedidaResponseDTO> criarUnidadeMedida(
            @RequestBody @Valid UnidadeMedidaRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        UnidadeMedida criada = unidadeMedidaService.criarUnidadeMedida(unidadeMedidaAssembler.toEntity(request));
        UnidadeMedidaResponseDTO response = unidadeMedidaAssembler.toResponseDTO(criada);
        URI uri = uriBuilder.path("/api/unidades-medida/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadeMedidaResponseDTO> atualizarUnidadeMedida(
            @PathVariable Long id,
            @RequestBody @Valid UnidadeMedidaRequestDTO request) {
        UnidadeMedida atualizada = unidadeMedidaService.atualizarUnidadeMedida(
                id, unidadeMedidaAssembler.toEntity(request));
        return ResponseEntity.ok(unidadeMedidaAssembler.toResponseDTO(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUnidadeMedida(@PathVariable Long id) {
        unidadeMedidaService.deletarUnidadeMedida(id);
        return ResponseEntity.noContent().build();
    }
}
