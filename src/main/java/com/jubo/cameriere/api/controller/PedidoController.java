package com.jubo.cameriere.api.controller;

import com.jubo.cameriere.api.assembler.PedidoAssembler;
import com.jubo.cameriere.api.dto.*;
import com.jubo.cameriere.domain.model.Pedido;
import com.jubo.cameriere.domain.model.StatusPedido;
import com.jubo.cameriere.domain.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoAssembler pedidoAssembler;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(
            @Valid @RequestBody PedidoRequestDTO request,
            UriComponentsBuilder uriBuilder) {

        Pedido pedido = pedidoService.criarPedido(pedidoAssembler.toCreateCommand(request));
        PedidoResponseDTO response = pedidoAssembler.toResponseDTO(pedido);
        URI uri = uriBuilder.path("/api/pedidos/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoAssembler.toResponseDTO(pedidoService.buscarPedido(id)));
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmacaoRequestDTO request) {
        return ResponseEntity.ok(pedidoAssembler.toResponseDTO(
                pedidoService.confirmarPedido(id, request.getDataPrevisaoEntrega())));
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
        return ResponseEntity.ok(pedidoAssembler.toResponseDTO(pedidoService.marcarComoEntregue(id)));
    }

    @PutMapping("/{id}/empresa-nota")
    public ResponseEntity<PedidoResponseDTO> atualizarEmpresaNota(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoAssembler.toResponseDTO(pedidoService.atualizarEmpresaNota(id, empresaId)));
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidos(
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) Long inicioTimestamp,
            @RequestParam(required = false) Long fimTimestamp) {
        Long filtroId = pedidoId != null ? pedidoId : id;
        Page<PedidoResponseDTO> response = pedidoService
                .listarPedidos(pageable, status, clienteId, filtroId, inicioTimestamp, fimTimestamp)
                .map(pedidoAssembler::toResponseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/relatorios/resumo-filtros")
    public ResponseEntity<PedidoFiltroResumoDTO> resumirPedidosFiltrados(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) Long inicioTimestamp,
            @RequestParam(required = false) Long fimTimestamp) {
        Long filtroId = pedidoId != null ? pedidoId : id;
        return ResponseEntity.ok(pedidoAssembler.toPedidoFiltroResumoDTO(
                pedidoService.resumirPedidosFiltrados(status, clienteId, filtroId, inicioTimestamp, fimTimestamp)));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PedidoResponseDTO> response = pedidoService.listarPedidosPorCliente(clienteId, pageable)
                .map(pedidoAssembler::toResponseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosPorStatus(
            @PathVariable StatusPedido status,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PedidoResponseDTO> response = pedidoService.listarPedidosPorStatus(status, pageable)
                .map(pedidoAssembler::toResponseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/relatorios/valor-total-periodo")
    public ResponseEntity<ValorTotalPeriodoDTO> calcularValorTotalPedidosNoPeriodo(
            @RequestParam Long inicioTimestamp,
            @RequestParam Long fimTimestamp) {
        return ResponseEntity.ok(pedidoAssembler.toValorTotalPeriodoDTO(
                pedidoService.calcularValorTotalPedidosNoPeriodo(inicioTimestamp, fimTimestamp)));
    }

    @GetMapping("/relatorios/pedidos-por-cliente")
    public ResponseEntity<List<PedidosPorClienteDTO>> gerarRelatorioPedidosPorCliente(
            @RequestParam(required = false) Long inicioTimestamp,
            @RequestParam(required = false) Long fimTimestamp) {
        List<PedidosPorClienteDTO> response = pedidoService
                .gerarRelatorioPedidosPorCliente(inicioTimestamp, fimTimestamp).stream()
                .map(pedidoAssembler::toPedidosPorClienteDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/relatorios/compras")
    public ResponseEntity<List<CompraProdutoPeriodoDTO>> gerarRelatorioCompras(
            @RequestParam Long inicioTimestamp,
            @RequestParam Long fimTimestamp) {
        List<CompraProdutoPeriodoDTO> response = pedidoService
                .gerarRelatorioCompras(inicioTimestamp, fimTimestamp).stream()
                .map(pedidoAssembler::toCompraProdutoPeriodoDTO)
                .toList();
        return ResponseEntity.ok(response);
    }
}
