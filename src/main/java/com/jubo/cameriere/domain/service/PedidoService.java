package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.api.exceptions.PedidoNaoEncontradoException;
import com.jubo.cameriere.api.exceptions.PedidoStatusInvalidoException;
import com.jubo.cameriere.domain.dto.PedidoRequestDTO;
import com.jubo.cameriere.domain.dto.PedidoResponseDTO;
import com.jubo.cameriere.domain.dto.ItemPedidoRequestDTO;
import com.jubo.cameriere.domain.model.*;
import com.jubo.cameriere.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EstanteProdutoRepository estanteProdutoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        log.info("Criando novo pedido para cliente: {}", request.clienteId());

        // 1. Busca e valida o cliente
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + request.clienteId()));

        if (!cliente.getAtivo()) {
            throw new IllegalStateException("Cliente inativo não pode realizar pedidos");
        }

        // 2. Cria o pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setEntregue(false);
        pedido.setFinalizado(false);
        pedido.setObservacao(request.observacao());
        pedido.setValorTotal(BigDecimal.ZERO);

        // 3. Salva o pedido primeiro para gerar o ID
        pedido = pedidoRepository.save(pedido);

        // 4. Processa os itens
        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoRequestDTO itemRequest : request.itens()) {
            // Busca o produto na estante
            EstanteProduto estanteProduto = estanteProdutoRepository
                    .findByEstanteIdAndProdutoId(itemRequest.estanteId(), itemRequest.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Produto %d não encontrado na estante %d",
                                    itemRequest.produtoId(), itemRequest.estanteId())
                    ));

            // Verifica se está ativo
            if (!estanteProduto.getAtivo()) {
                throw new IllegalStateException(
                        String.format("Produto %s está inativo na estante", estanteProduto.getProduto().getNome())
                );
            }

            // Verifica disponibilidade de estoque
            if (!estanteProduto.isDisponivel()) {
                throw new IllegalStateException(
                        String.format("Produto %s indisponível",
                                estanteProduto.getProduto().getNome())
                );
            }

            // Cria o item do pedido
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(estanteProduto.getProduto());
            item.setEstanteProduto(estanteProduto);
            item.setQuantidade(itemRequest.quantidade());
            item.setObservacao(itemRequest.observacao());

            // Calcula o preço total do item
            BigDecimal precoTotal = estanteProduto.getPrecoVenda().multiply(itemRequest.quantidade());
            item.setPrecoTotal(BigDecimal.valueOf(precoTotal.doubleValue()));

            itens.add(item);
            valorTotal = valorTotal.add(precoTotal);
        }

        // 5. Salva todos os itens
        itemPedidoRepository.saveAll(itens);
        pedido.setItens(itens);
        pedido.setValorTotal(valorTotal);

        // 6. Atualiza o pedido com o valor total
        pedido = pedidoRepository.save(pedido);

        log.info("Pedido criado com sucesso: ID={}, Cliente={}, Valor Total=R$ {}",
                pedido.getId(), cliente.getNome(), valorTotal);

        return toResponseDTO(pedido);
    }

    public PedidoResponseDTO buscarPedido(Long id) {
        log.info("Buscando pedido: {}", id);

        Pedido pedido = pedidoRepository.findByIdWithItens(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));

        return toResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO confirmarPedido(Long id) {
        log.info("Confirmando pedido: {}", id);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));

        pedido.confirmar();

        Pedido saved = pedidoRepository.save(pedido);

        log.info("Pedido confirmado: ID={}", id);

        return toResponseDTO(saved);
    }

    @Transactional
    public void cancelarPedido(Long id, String motivo) {
        log.info("Cancelando pedido: {}, Motivo: {}", id, motivo);

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));

        pedido.cancelar(motivo);

        pedidoRepository.save(pedido);

        log.info("Pedido cancelado: ID={}", id);
    }

    @Transactional
    public PedidoResponseDTO marcarComoEntregue(Long id) {
        log.info("Marcando pedido como entregue: {}", id);

        Pedido pedido = pedidoRepository.findByIdWithItens(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));

        pedido.entregar();

        Pedido saved = pedidoRepository.save(pedido);

        log.info("Pedido marcado como entregue: ID={}", id);

        return toResponseDTO(saved);
    }

    public Page<PedidoResponseDTO> listarPedidos(Pageable pageable, StatusPedido status, Long clienteId) {
        log.info("Listando pedidos - Status: {}, ClienteId: {}", status, clienteId);

        Page<Pedido> pedidos;

        if (status != null && clienteId != null) {
            pedidos = pedidoRepository.findByClienteIdAndStatus(clienteId, status, pageable);
        } else if (status != null) {
            pedidos = pedidoRepository.findByStatus(status, pageable);
        } else if (clienteId != null) {
            pedidos = pedidoRepository.findByClienteId(clienteId, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        return pedidos.map(this::toResponseDTO);
    }

    public Page<PedidoResponseDTO> listarPedidosPorCliente(Long clienteId, Pageable pageable) {
        log.info("Listando pedidos do cliente: {}", clienteId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + clienteId));

        Page<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId, pageable);

        return pedidos.map(this::toResponseDTO);
    }

    public Page<PedidoResponseDTO> listarPedidosPorStatus(StatusPedido status, Pageable pageable) {
        log.info("Listando pedidos por status: {}", status);

        Page<Pedido> pedidos = pedidoRepository.findByStatus(status, pageable);

        return pedidos.map(this::toResponseDTO);
    }

    public Double calcularValorTotalPedidosNoPeriodo(Long inicioTimestamp, Long fimTimestamp) {
        log.info("Calculando valor total de pedidos no período: {} até {}", inicioTimestamp, fimTimestamp);

        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);

        Double valorTotal = pedidoRepository.sumValorTotalByPeriodo(inicio, fim);

        return valorTotal != null ? valorTotal : 0.0;
    }

    public List<Object[]> gerarRelatorioPedidosPorCliente() {
        log.info("Gerando relatório de pedidos por cliente");

        return pedidoRepository.countPedidosByCliente();
    }

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getDataCriacao(),
                pedido.getDataConfirmacao(),
                pedido.getDataCancelamento(),
                pedido.getDataEntrega(),
                pedido.getValorTotal(),
                pedido.getEntregue(),
                pedido.getObservacao(),
                pedido.getObsCancelamento(),
                pedido.getFinalizado(),
                toClienteResumoDTO(pedido.getCliente()),
                toItensResponseDTO(pedido.getItens())
        );
    }

    private PedidoResponseDTO.ClienteResumoDTO toClienteResumoDTO(Cliente cliente) {
        if (cliente == null) return null;

        return new PedidoResponseDTO.ClienteResumoDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCnpj()
        );
    }

    private List<PedidoResponseDTO.ItemPedidoResponseDTO> toItensResponseDTO(List<ItemPedido> itens) {
        if (itens == null) return List.of();

        return itens.stream()
                .map(this::toItemResponseDTO)
                .toList();
    }

    private PedidoResponseDTO.ItemPedidoResponseDTO toItemResponseDTO(ItemPedido item) {
        if (item == null) return null;

        BigDecimal precoUnitario = BigDecimal.ZERO;
        if (item.getQuantidade() != null && item.getQuantidade().compareTo(BigDecimal.ZERO) != 0) {
            precoUnitario = item.getPrecoTotal()
                    .divide(item.getQuantidade(), 2, BigDecimal.ROUND_HALF_UP);
        }

        return new PedidoResponseDTO.ItemPedidoResponseDTO(
                item.getId(),
                item.getQuantidade(),
                item.getPrecoTotal(),
                precoUnitario,
                item.getObservacao(),
                toProdutoResumoDTO(item.getProduto())
        );
    }

    private PedidoResponseDTO.ItemPedidoResponseDTO.ProdutoResumoDTO toProdutoResumoDTO(Produto produto) {
        if (produto == null) return null;

        String unidadeMedida = produto.getUnidadeMedida() != null ?
                produto.getUnidadeMedida().getSigla() : "UN";

        return new PedidoResponseDTO.ItemPedidoResponseDTO.ProdutoResumoDTO(
                produto.getId(),
                produto.getNome(),
                unidadeMedida
        );
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}