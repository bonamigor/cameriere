package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.EmpresaAssembler;
import com.jubo.cameriere.api.dto.EmpresaRequestDTO;
import com.jubo.cameriere.api.dto.EmpresaResponseDTO;
import com.jubo.cameriere.domain.model.Empresa;
import com.jubo.cameriere.domain.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaAssembler empresaAssembler;

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDTO>> listarTodasEmpresas() {
        List<EmpresaResponseDTO> response = empresaService.listarTodasEmpresas().stream()
                .map(empresaAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empresaAssembler.toResponseDTO(empresaService.buscarEmpresaPorId(id)));
    }

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> criarEmpresa(
            @RequestBody @Valid EmpresaRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        Empresa criada = empresaService.criarEmpresa(empresaAssembler.toEntity(request));
        EmpresaResponseDTO response = empresaAssembler.toResponseDTO(criada);
        URI uri = uriBuilder.path("/api/empresas/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> atualizarEmpresa(
            @PathVariable Long id,
            @RequestBody @Valid EmpresaRequestDTO request) {
        Empresa atualizada = empresaService.atualizarEmpresa(id, empresaAssembler.toEntity(request));
        return ResponseEntity.ok(empresaAssembler.toResponseDTO(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEmpresa(@PathVariable Long id) {
        empresaService.deletarEmpresa(id);
        return ResponseEntity.noContent().build();
    }
}
