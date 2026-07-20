package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.EstanteProdutoAssembler;
import com.jubo.cameriere.api.dto.*;
import com.jubo.cameriere.domain.model.EstanteProduto;
import com.jubo.cameriere.domain.service.EstanteProdutoService;
import com.jubo.cameriere.domain.service.comando.EstanteProdutoUpdateCommand;
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
public class EstanteProdutoController {

    private final EstanteProdutoService estanteProdutoService;
    private final EstanteProdutoAssembler estanteProdutoAssembler;

    @PostMapping
    public ResponseEntity<EstanteProdutoResponseDTO> associarProduto(
            @Valid @RequestBody EstanteProdutoRequestDTO request,
            UriComponentsBuilder uriBuilder) {

        EstanteProduto criada = estanteProdutoService.associarProdutoEstante(
                estanteProdutoAssembler.toAssociateCommand(request));
        EstanteProdutoResponseDTO response = estanteProdutoAssembler.toResponseDTO(criada);

        URI uri = uriBuilder.path("/api/estantes-produtos/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstanteProdutoResponseDTO> atualizarAssociacao(
            @PathVariable Long id,
            @Valid @RequestBody EstanteProdutoUpdateDTO updateDTO) {

        EstanteProduto atualizada = estanteProdutoService.atualizarAssociacao(
                id, estanteProdutoAssembler.toUpdateCommand(updateDTO));
        return ResponseEntity.ok(estanteProdutoAssembler.toResponseDTO(atualizada));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desativarAssociacao(@PathVariable Long id) {
        estanteProdutoService.atualizarAssociacao(id, new EstanteProdutoUpdateCommand(null, null, false));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/preco")
    public ResponseEntity<EstanteProdutoResponseDTO> ajustarPreco(
            @PathVariable Long id,
            @RequestParam BigDecimal novoPreco,
            @RequestParam(required = false) String motivo) {

        EstanteProduto atualizada = estanteProdutoService.ajustarPreco(id, novoPreco, motivo);
        return ResponseEntity.ok(estanteProdutoAssembler.toResponseDTO(atualizada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstanteProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estanteProdutoAssembler.toResponseDTO(estanteProdutoService.buscarPorId(id)));
    }

    @GetMapping("/estante/{estanteId}")
    public ResponseEntity<Page<EstanteProdutoResponseDTO>> listarPorEstante(
            @PathVariable Long estanteId,
            @PageableDefault(size = 20, sort = "produto.nome", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<EstanteProdutoResponseDTO> produtos = estanteProdutoService.listarPorEstante(estanteId, pageable)
                .map(estanteProdutoAssembler::toResponseDTO);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/estante/{estanteId}/disponiveis")
    public ResponseEntity<List<EstanteProdutoResponseDTO>> listarProdutosDisponiveis(
            @PathVariable Long estanteId) {

        List<EstanteProdutoResponseDTO> produtos = estanteProdutoService.buscarProdutosDisponiveis(estanteId).stream()
                .map(estanteProdutoAssembler::toResponseDTO)
                .toList();
        return ResponseEntity.ok(produtos);
    }
}
