package com.jubo.cameriere.domain.controller;

import com.jubo.cameriere.domain.dto.CancelamentoRequestDTO;
import com.jubo.cameriere.domain.dto.PedidoRequestDTO;
import com.jubo.cameriere.domain.dto.PedidoResponseDTO;
import com.jubo.cameriere.domain.model.StatusPedido;
import com.jubo.cameriere.domain.service.PedidoService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PedidoController {

    private final PedidoService pedidoService;

    // CRUD Principal
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(
            @Valid @RequestBody PedidoRequestDTO request,
            UriComponentsBuilder uriBuilder) {

        PedidoResponseDTO response = pedidoService.criarPedido(request);

        URI uri = uriBuilder.path("/api/pedidos/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.buscarPedido(id);
        return ResponseEntity.ok(response);
    }

    // Operações de Status
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.confirmarPedido(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelarPedido(
            @PathVariable Long id,
            @Valid @RequestBody CancelamentoRequestDTO request) {

        pedidoService.cancelarPedido(id, request.getMotivo());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponseDTO> marcarComoEntregue(@PathVariable Long id) {
        PedidoResponseDTO response = pedidoService.marcarComoEntregue(id);
        return ResponseEntity.ok(response);
    }

    // Consultas
    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidos(
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long clienteId) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(pageable, status, clienteId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorCliente(clienteId, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPorStatus(
            @PathVariable StatusPedido status,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidosPorStatus(status, pageable);
        return ResponseEntity.ok(pedidos);
    }

    // Relatórios
    @GetMapping("/relatorios/valor-total-periodo")
    public ResponseEntity<Double> calcularValorTotalPedidosNoPeriodo(
            @RequestParam Long inicioTimestamp,
            @RequestParam Long fimTimestamp) {

        Double valorTotal = pedidoService.calcularValorTotalPedidosNoPeriodo(inicioTimestamp, fimTimestamp);
        return ResponseEntity.ok(valorTotal);
    }

    @GetMapping("/relatorios/pedidos-por-cliente")
    public ResponseEntity<List<Object[]>> gerarRelatorioPedidosPorCliente() {
        List<Object[]> relatorio = pedidoService.gerarRelatorioPedidosPorCliente();
        return ResponseEntity.ok(relatorio);
    }
}