package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Pedido;
import com.jubo.cameriere.domain.model.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {
    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);

    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    Page<Pedido> findByClienteIdAndStatus(Long clienteId, StatusPedido status, Pageable pageable);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByStatus(StatusPedido status);

    @Query("SELECT DISTINCT p FROM Pedido p " +
            "LEFT JOIN FETCH p.itens i " +
            "LEFT JOIN FETCH i.produto prod " +
            "LEFT JOIN FETCH prod.unidadeMedida " +
            "LEFT JOIN FETCH i.estanteProduto " +
            "LEFT JOIN FETCH p.cliente c " +
            "LEFT JOIN FETCH c.empresa " +
            "LEFT JOIN FETCH p.empresaNota " +
            "WHERE p.id = :id")
    Optional<Pedido> findByIdWithItens(@Param("id") Long id);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.cliente WHERE p.id = :id")
    Optional<Pedido> findByIdWithCliente(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens i LEFT JOIN FETCH i.produto WHERE p.cliente.id = :clienteId")
    List<Pedido> findByClienteIdWithItens(@Param("clienteId") Long clienteId);

    // ===== FILTROS COMBINADOS =====

    @Query("""
            SELECT p FROM Pedido p
            WHERE (:id IS NULL OR p.id = :id)
              AND (:status IS NULL OR p.status = :status)
              AND (:clienteId IS NULL OR p.cliente.id = :clienteId)
              AND (:inicio IS NULL OR p.dataCriacao >= :inicio)
              AND (:fim IS NULL OR p.dataCriacao <= :fim)
            """)
    Page<Pedido> findFiltrado(
            @Param("id") Long id,
            @Param("status") StatusPedido status,
            @Param("clienteId") Long clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable);

    @Query("""
            SELECT COUNT(p), COALESCE(SUM(p.valorTotal), 0)
            FROM Pedido p
            WHERE (:id IS NULL OR p.id = :id)
              AND (:status IS NULL OR p.status = :status)
              AND (:clienteId IS NULL OR p.cliente.id = :clienteId)
              AND (:inicio IS NULL OR p.dataCriacao >= :inicio)
              AND (:fim IS NULL OR p.dataCriacao <= :fim)
            """)
    List<Object[]> resumirFiltrado(
            @Param("id") Long id,
            @Param("status") StatusPedido status,
            @Param("clienteId") Long clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT p.cliente.id, p.cliente.nome, COUNT(p), COALESCE(SUM(p.valorTotal), 0)
            FROM Pedido p
            WHERE (:inicio IS NULL OR p.dataCriacao >= :inicio)
              AND (:fim IS NULL OR p.dataCriacao <= :fim)
            GROUP BY p.cliente.id, p.cliente.nome
            ORDER BY SUM(p.valorTotal) DESC
            """)
    List<Object[]> countPedidosByClienteNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT i.produto.id,
                   i.produto.nome,
                   COALESCE(um.sigla, 'UN'),
                   SUM(i.quantidade),
                   COALESCE(SUM(i.precoTotal), 0),
                   COUNT(DISTINCT p.id)
            FROM ItemPedido i
            JOIN i.pedido p
            JOIN i.produto prod
            LEFT JOIN prod.unidadeMedida um
            WHERE p.status IN :statuses
              AND p.dataConfirmacao IS NOT NULL
              AND p.dataConfirmacao >= :inicio
              AND p.dataConfirmacao <= :fim
            GROUP BY i.produto.id, i.produto.nome, um.sigla
            ORDER BY SUM(i.precoTotal) DESC
            """)
    List<Object[]> relatorioComprasPorProduto(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("statuses") List<StatusPedido> statuses);

    // ===== CONSULTAS AGREGADAS =====

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim AND p.status = 'CONFIRMADO'")
    Double sumValorTotalByPeriodo(@Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim);

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.cliente.id = :clienteId AND p.status = 'CONFIRMADO'")
    Double sumValorTotalByCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.cliente.id = :clienteId")
    Integer countByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status = :status")
    Integer countByStatus(@Param("status") StatusPedido status);

    // ===== RELATÓRIOS =====

    @Query("SELECT p.cliente.id, p.cliente.nome, COUNT(p), SUM(p.valorTotal) " +
            "FROM Pedido p " +
            "GROUP BY p.cliente.id, p.cliente.nome " +
            "ORDER BY SUM(p.valorTotal) DESC")
    List<Object[]> countPedidosByCliente();

    @Query("SELECT FUNCTION('DATE', p.dataCriacao) as data, " +
            "COUNT(p), SUM(p.valorTotal), AVG(p.valorTotal) " +
            "FROM Pedido p " +
            "WHERE p.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY FUNCTION('DATE', p.dataCriacao) " +
            "ORDER BY data DESC")
    List<Object[]> getRelatorioDiarioPedidos(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);

    @Query("SELECT p.status, COUNT(p), SUM(p.valorTotal) " +
            "FROM Pedido p " +
            "GROUP BY p.status")
    List<Object[]> getRelatorioPorStatus();

    @Query("SELECT FUNCTION('YEAR', p.dataCriacao) as ano, " +
            "FUNCTION('MONTH', p.dataCriacao) as mes, " +
            "COUNT(p), SUM(p.valorTotal) " +
            "FROM Pedido p " +
            "WHERE p.status = 'CONFIRMADO' " +
            "GROUP BY FUNCTION('YEAR', p.dataCriacao), FUNCTION('MONTH', p.dataCriacao) " +
            "ORDER BY ano DESC, mes DESC")
    List<Object[]> getRelatorioMensalPedidos();

    // ===== CONSULTAS POR PERÍODO =====

    List<Pedido> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT p FROM Pedido p WHERE p.dataCriacao BETWEEN :inicio AND :fim AND p.status = :status")
    List<Pedido> findByPeriodoAndStatus(@Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim,
                                        @Param("status") StatusPedido status);

    // ===== ÚLTIMOS PEDIDOS =====

    @Query("SELECT p FROM Pedido p ORDER BY p.dataCriacao DESC LIMIT 10")
    List<Pedido> findUltimosPedidos();

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.dataCriacao DESC")
    List<Pedido> findUltimosPedidosByCliente(@Param("clienteId") Long clienteId);

    // ===== VERIFICAÇÕES =====

    boolean existsByIdAndClienteId(Long id, Long clienteId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Pedido p WHERE p.id = :id AND p.status = :status")
    boolean isPedidoWithStatus(@Param("id") Long id, @Param("status") StatusPedido status);

    // ===== OPERAÇÕES DE ATUALIZAÇÃO EM MASSA =====

    @Modifying
    @Query("UPDATE Pedido p SET p.status = :status, p.dataCancelamento = CURRENT_TIMESTAMP " +
            "WHERE p.id = :id AND p.status = 'PENDENTE'")
    int cancelarPedidoPendente(@Param("id") Long id, @Param("status") StatusPedido status);

    @Modifying
    @Query("UPDATE Pedido p SET p.finalizado = true WHERE p.id = :id AND p.status = 'ENTREGUE'")
    int finalizarPedido(@Param("id") Long id);
}