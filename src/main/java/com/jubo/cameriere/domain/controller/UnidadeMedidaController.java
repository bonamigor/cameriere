package com.jubo.cameriere.domain.controller;

import com.jubo.cameriere.domain.model.UnidadeMedida;
import com.jubo.cameriere.domain.service.UnidadeMedidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
public class UnidadeMedidaController {

    private final UnidadeMedidaService unidadeMedidaService;

    public UnidadeMedidaController(UnidadeMedidaService unidadeMedidaService) {
        this.unidadeMedidaService = unidadeMedidaService;
    }

    @GetMapping
    public ResponseEntity<List<UnidadeMedida>> listarTodasUnidadesMedida() {
        List<UnidadeMedida> unidades = unidadeMedidaService.listarTodasUnidadesMedida();
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadeMedida> buscarUnidadeMedidaPorId(@PathVariable Long id) {
        return unidadeMedidaService.buscarUnidadeMedidaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UnidadeMedida> criarUnidadeMedida(@RequestBody @Valid UnidadeMedida unidadeMedida) {
        UnidadeMedida novaUnidade = unidadeMedidaService.criarUnidadeMedida(unidadeMedida);
        return ResponseEntity.status(201).body(novaUnidade);
    }

    @PutMapping
    public ResponseEntity<UnidadeMedida> atualizarUnidadeMedida(@RequestBody @Valid UnidadeMedida unidadeMedida) {
        UnidadeMedida unidadeAtualizada = unidadeMedidaService.atualizarUnidadeMedida(unidadeMedida);
        return ResponseEntity.ok(unidadeAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUnidadeMedida(@PathVariable Long id) {
        unidadeMedidaService.deletarUnidadeMedida(id);
        return ResponseEntity.noContent().build();
    }
}