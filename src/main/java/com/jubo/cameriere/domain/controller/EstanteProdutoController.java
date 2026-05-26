package com.jubo.cameriere.domain.controller;

import com.jubo.cameriere.domain.dto.*;
import com.jubo.cameriere.domain.service.EstanteProdutoService;
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
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/estantes-produtos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EstanteProdutoController {

    private final EstanteProdutoService estanteProdutoService;

    // Endpoints de CRUD
    @PostMapping
    public ResponseEntity<EstanteProdutoResponseDTO> associarProduto(
            @Valid @RequestBody EstanteProdutoRequestDTO request,
            UriComponentsBuilder uriBuilder) {

        EstanteProdutoResponseDTO response = estanteProdutoService.associarProdutoEstante(request);

        URI uri = uriBuilder.path("/api/estantes-produtos/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstanteProdutoResponseDTO> atualizarAssociacao(
            @PathVariable Long id,
            @Valid @RequestBody EstanteProdutoUpdateDTO updateDTO) {

        EstanteProdutoResponseDTO response = estanteProdutoService.atualizarAssociacao(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desativarAssociacao(@PathVariable Long id) {
        // Desativa a associação (não remove fisicamente)
        EstanteProdutoUpdateDTO updateDTO = new EstanteProdutoUpdateDTO();
        updateDTO.setAtivo(false);
        estanteProdutoService.atualizarAssociacao(id, updateDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/preco")
    public ResponseEntity<EstanteProdutoResponseDTO> ajustarPreco(
            @PathVariable Long id,
            @RequestParam BigDecimal novoPreco,
            @RequestParam(required = false) String motivo) {

        EstanteProdutoResponseDTO response = estanteProdutoService.ajustarPreco(id, novoPreco, motivo);
        return ResponseEntity.ok(response);
    }

    // Endpoints de Consulta
    @GetMapping("/{id}")
    public ResponseEntity<EstanteProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        EstanteProdutoResponseDTO response = estanteProdutoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estante/{estanteId}")
    public ResponseEntity<Page<EstanteProdutoResponseDTO>> listarPorEstante(
            @PathVariable Long estanteId,
            @PageableDefault(size = 20, sort = "produto.nome", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EstanteProdutoResponseDTO> produtos = estanteProdutoService.listarPorEstante(estanteId, pageable);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/estante/{estanteId}/disponiveis")
    public ResponseEntity<List<EstanteProdutoResponseDTO>> listarProdutosDisponiveis(
            @PathVariable Long estanteId) {

        List<EstanteProdutoResponseDTO> produtos = estanteProdutoService.buscarProdutosDisponiveis(estanteId);
        return ResponseEntity.ok(produtos);
    }
}
