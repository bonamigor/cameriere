package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.ProdutoAssembler;
import com.jubo.cameriere.api.dto.ProdutoRequestDTO;
import com.jubo.cameriere.api.dto.ProdutoResponseDTO;
import com.jubo.cameriere.domain.model.Produto;
import com.jubo.cameriere.domain.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoAssembler produtoAssembler;

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodosProdutos() {
        List<ProdutoResponseDTO> response = produtoService.listarTodosProdutos().stream()
                .map(produtoAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoAssembler.toResponseDTO(produtoService.buscarProdutoPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criarProduto(
            @RequestBody @Valid ProdutoRequestDTO request,
            UriComponentsBuilder uriBuilder) {
        Produto criado = produtoService.criarProduto(
                produtoAssembler.toEntity(request),
                request.unidadeMedidaSigla());
        ProdutoResponseDTO response = produtoAssembler.toResponseDTO(criado);
        URI uri = uriBuilder.path("/api/produtos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @PathVariable Long id,
            @RequestBody @Valid ProdutoRequestDTO request) {
        Produto atualizado = produtoService.atualizarProduto(
                id,
                produtoAssembler.toEntity(request),
                request.unidadeMedidaSigla());
        return ResponseEntity.ok(produtoAssembler.toResponseDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
