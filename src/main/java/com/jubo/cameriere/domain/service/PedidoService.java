package com.jubo.cameriere.domain.service;

import com.jubo.cameriere.domain.exception.*;
import com.jubo.cameriere.domain.model.*;
import com.jubo.cameriere.domain.repository.*;
import com.jubo.cameriere.domain.service.comando.CompraProdutoPeriodo;
import com.jubo.cameriere.domain.service.comando.ItemPedidoCommand;
import com.jubo.cameriere.domain.service.comando.PedidoCreateCommand;
import com.jubo.cameriere.domain.service.comando.PedidoFiltroResumo;
import com.jubo.cameriere.domain.service.comando.PedidosPorCliente;
import com.jubo.cameriere.domain.service.comando.ValorTotalPeriodo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private final EmpresaRepository empresaRepository;
    private final EstanteProdutoRepository estanteProdutoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final EntityManager entityManager;

    @Transactional
    public Pedido criarPedido(PedidoCreateCommand comando) {
        log.info("Criando novo pedido para cliente: {}", comando.clienteId());

        Cliente cliente = clienteRepository.findById(comando.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado: " + comando.clienteId()));

        if (!Boolean.TRUE.equals(cliente.getAtivo())) {
            throw new ClienteInativoException("Cliente inativo não pode realizar pedidos");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setEntregue(false);
        pedido.setFinalizado(false);
        pedido.setObservacao(comando.observacao());
        pedido.setValorTotal(BigDecimal.ZERO);

        pedido = pedidoRepository.save(pedido);

        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ItemPedidoCommand itemCommand : comando.itens()) {
            EstanteProduto estanteProduto = estanteProdutoRepository
                    .findByEstanteIdAndProdutoId(itemCommand.estanteId(), itemCommand.produtoId())
                    .orElseThrow(() -> new ProdutoNaoEncontradoException(
                            String.format("Produto %d não encontrado na estante %d",
                                    itemCommand.produtoId(), itemCommand.estanteId())
                    ));

            if (!Boolean.TRUE.equals(estanteProduto.getAtivo())) {
                throw new ProdutoInativoException(
                        String.format("Produto %s está inativo na estante", estanteProduto.getProduto().getNome())
                );
            }

            if (!estanteProduto.isDisponivel() || !estanteProduto.possuiEstoqueSuficiente(itemCommand.quantidade())) {
                throw new EstoqueInsuficienteException(
                        String.format("Produto %s sem estoque suficiente. Disponível: %s",
                                estanteProduto.getProduto().getNome(),
                                estanteProduto.getQuantidade().stripTrailingZeros().toPlainString())
                );
            }

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(estanteProduto.getProduto());
            item.setEstanteProduto(estanteProduto);
            item.setQuantidade(itemCommand.quantidade());
            item.setObservacao(itemCommand.observacao());

            BigDecimal precoTotal = estanteProduto.getPrecoVenda().multiply(itemCommand.quantidade());
            item.setPrecoTotal(precoTotal);

            itens.add(item);
            valorTotal = valorTotal.add(precoTotal);
        }

        itemPedidoRepository.saveAll(itens);
        pedido.setItens(itens);
        pedido.setValorTotal(valorTotal);

        pedido = pedidoRepository.save(pedido);

        log.info("Pedido criado com sucesso: ID={}, Cliente={}, Valor Total=R$ {}",
                pedido.getId(), cliente.getNome(), valorTotal);

        return buscarPedido(pedido.getId());
    }

    public Pedido buscarPedido(Long id) {
        return pedidoRepository.findByIdWithItens(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    @Transactional
    public Pedido confirmarPedido(Long id, LocalDate dataPrevisaoEntrega) {
        Pedido pedido = buscarPedido(id);

        for (ItemPedido item : pedido.getItens()) {
            EstanteProduto estanteProduto = item.getEstanteProduto();
            if (!estanteProduto.possuiEstoqueSuficiente(item.getQuantidade())) {
                throw new EstoqueInsuficienteException(
                        String.format("Estoque insuficiente para confirmar o pedido. Produto: %s",
                                item.getProduto().getNome()));
            }
            estanteProduto.baixarEstoque(item.getQuantidade());
            estanteProdutoRepository.save(estanteProduto);
        }

        pedido.confirmar(dataPrevisaoEntrega);
        if (pedido.getEmpresaNota() == null && pedido.getCliente().getEmpresa() != null) {
            pedido.setEmpresaNota(pedido.getCliente().getEmpresa());
        }
        pedidoRepository.save(pedido);

        log.info("Pedido confirmado: ID={}, previsão de entrega={}", id, dataPrevisaoEntrega);
        return buscarPedido(id);
    }

    @Transactional
    public Pedido atualizarEmpresaNota(Long id, Long empresaId) {
        Pedido pedido = buscarPedido(id);
        if (pedido.getStatus() != StatusPedido.CONFIRMADO) {
            throw new PedidoStatusInvalidoException("A empresa da nota só pode ser alterada em pedidos confirmados");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada: " + empresaId));
        pedido.setEmpresaNota(empresa);
        pedidoRepository.save(pedido);

        log.info("Empresa da nota atualizada: pedido={}, empresa={}", id, empresaId);
        return buscarPedido(id);
    }

    @Transactional
    public void cancelarPedido(Long id, String motivo) {
        Pedido pedido = buscarPedido(id);

        StatusPedido statusAnterior = pedido.getStatus();
        pedido.cancelar(motivo);

        if (statusAnterior == StatusPedido.CONFIRMADO) {
            for (ItemPedido item : pedido.getItens()) {
                EstanteProduto estanteProduto = item.getEstanteProduto();
                estanteProduto.reporEstoque(item.getQuantidade());
                estanteProdutoRepository.save(estanteProduto);
            }
        }

        pedidoRepository.save(pedido);
        log.info("Pedido cancelado: ID={}", id);
    }

    @Transactional
    public Pedido marcarComoEntregue(Long id) {
        Pedido pedido = buscarPedido(id);

        pedido.entregar();
        pedidoRepository.save(pedido);

        log.info("Pedido marcado como entregue: ID={}", id);
        return buscarPedido(id);
    }

    public Page<Pedido> listarPedidos(
            Pageable pageable,
            StatusPedido status,
            Long clienteId,
            Long id,
            Long inicioTimestamp,
            Long fimTimestamp) {
        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);
        Page<Pedido> page = pedidoRepository.findAll(
                PedidoSpecifications.comFiltros(id, status, clienteId, inicio, fim),
                pageable);
        page.forEach(this::inicializarParaResposta);
        return page;
    }

    public PedidoFiltroResumo resumirPedidosFiltrados(
            StatusPedido status,
            Long clienteId,
            Long id,
            Long inicioTimestamp,
            Long fimTimestamp) {
        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);
        Specification<Pedido> spec = PedidoSpecifications.comFiltros(id, status, clienteId, inicio, fim);

        long quantidade = pedidoRepository.count(spec);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Pedido> root = cq.from(Pedido.class);
        cq.select(cb.coalesce(cb.sum(root.get("valorTotal")), BigDecimal.ZERO));
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }
        BigDecimal valor = entityManager.createQuery(cq).getSingleResult();

        return new PedidoFiltroResumo(quantidade, valor != null ? valor : BigDecimal.ZERO);
    }

    public Page<Pedido> listarPedidosPorCliente(Long clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNaoEncontradoException("Cliente não encontrado: " + clienteId);
        }
        Page<Pedido> page = pedidoRepository.findByClienteId(clienteId, pageable);
        page.forEach(this::inicializarParaResposta);
        return page;
    }

    public Page<Pedido> listarPedidosPorStatus(StatusPedido status, Pageable pageable) {
        Page<Pedido> page = pedidoRepository.findByStatus(status, pageable);
        page.forEach(this::inicializarParaResposta);
        return page;
    }

    public ValorTotalPeriodo calcularValorTotalPedidosNoPeriodo(Long inicioTimestamp, Long fimTimestamp) {
        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);
        Double valorTotal = pedidoRepository.sumValorTotalByPeriodo(inicio, fim);

        return new ValorTotalPeriodo(
                inicio,
                fim,
                BigDecimal.valueOf(valorTotal != null ? valorTotal : 0.0)
        );
    }

    public List<PedidosPorCliente> gerarRelatorioPedidosPorCliente(Long inicioTimestamp, Long fimTimestamp) {
        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);
        return pedidoRepository.countPedidosByClienteNoPeriodo(inicio, fim).stream()
                .map(row -> new PedidosPorCliente(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO
                ))
                .toList();
    }

    public List<CompraProdutoPeriodo> gerarRelatorioCompras(Long inicioTimestamp, Long fimTimestamp) {
        LocalDateTime inicio = toLocalDateTime(inicioTimestamp);
        LocalDateTime fim = toLocalDateTime(fimTimestamp);
        if (inicio == null || fim == null) {
            throw new NegocioException("Informe data inicial e final para o relatório de compras");
        }
        if (fim.isBefore(inicio)) {
            throw new NegocioException("Data final deve ser maior ou igual à data inicial");
        }

        List<StatusPedido> statuses = List.of(StatusPedido.CONFIRMADO, StatusPedido.ENTREGUE);
        return pedidoRepository.relatorioComprasPorProduto(inicio, fim, statuses).stream()
                .map(row -> new CompraProdutoPeriodo(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        row[2] != null ? row[2].toString() : "UN",
                        row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO,
                        row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO,
                        row[5] != null ? ((Number) row[5]).longValue() : 0L
                ))
                .toList();
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    private void inicializarParaResposta(Pedido pedido) {
        org.hibernate.Hibernate.initialize(pedido.getCliente());
        if (pedido.getCliente() != null) {
            org.hibernate.Hibernate.initialize(pedido.getCliente().getEmpresa());
        }
        org.hibernate.Hibernate.initialize(pedido.getEmpresaNota());
        org.hibernate.Hibernate.initialize(pedido.getItens());
        if (pedido.getItens() == null) {
            return;
        }
        for (ItemPedido item : pedido.getItens()) {
            org.hibernate.Hibernate.initialize(item.getProduto());
            if (item.getProduto() != null) {
                org.hibernate.Hibernate.initialize(item.getProduto().getUnidadeMedida());
            }
            org.hibernate.Hibernate.initialize(item.getEstanteProduto());
        }
    }
}
