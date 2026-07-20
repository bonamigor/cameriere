package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.EstanteAssembler;
import com.jubo.cameriere.api.dto.EstanteRequestDTO;
import com.jubo.cameriere.api.dto.EstanteResponseDTO;
import com.jubo.cameriere.api.dto.EstanteUpdateDTO;
import com.jubo.cameriere.domain.model.Estante;
import com.jubo.cameriere.domain.service.EstanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estantes")
@RequiredArgsConstructor
@Slf4j
public class EstanteController {

    private final EstanteService estanteService;
    private final EstanteAssembler estanteAssembler;

    @PostMapping
    public ResponseEntity<EstanteResponseDTO> criarEstante(
            @Valid @RequestBody EstanteRequestDTO request,
            UriComponentsBuilder uriBuilder) {

        Estante criada = estanteService.criarEstante(estanteAssembler.toCreateCommand(request));
        EstanteResponseDTO response = estanteAssembler.toResponseDTO(criada);

        URI uri = uriBuilder.path("/api/estantes/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstanteResponseDTO> atualizarEstante(
            @PathVariable Long id,
            @Valid @RequestBody EstanteUpdateDTO updateDTO) {

        Estante atualizada = estanteService.atualizarEstante(id, estanteAssembler.toUpdateCommand(updateDTO));
        return ResponseEntity.ok(estanteAssembler.toResponseDTO(atualizada));
    }

    @PatchMapping("/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desativarEstante(@PathVariable Long id) {
        estanteService.desativarEstante(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletarEstante(@PathVariable Long id) {
        estanteService.deletarEstante(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstanteResponseDTO> buscarEstantePorId(@PathVariable Long id) {
        return ResponseEntity.ok(estanteAssembler.toResponseDTO(estanteService.buscarEstantePorId(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<EstanteResponseDTO>> listarEstantesPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20, sort = "periodo", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EstanteResponseDTO> estantes = estanteService.listarEstantesPorCliente(clienteId, pageable)
                .map(estanteAssembler::toResponseDTO);
        return ResponseEntity.ok(estantes);
    }

    @GetMapping("/periodo/{periodo:.+}")
    public ResponseEntity<List<EstanteResponseDTO>> listarEstantesAtivasPorPeriodo(
            @PathVariable String periodo) {
        List<EstanteResponseDTO> estantes = estanteService.listarEstantesAtivasPorPeriodo(periodo).stream()
                .map(estanteAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(estantes);
    }
}
