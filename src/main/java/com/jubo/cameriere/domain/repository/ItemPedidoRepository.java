package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.ItemPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    // ===== BUSCAS BÁSICAS =====

    List<ItemPedido> findByPedidoId(Long pedidoId);

    Page<ItemPedido> findByPedidoId(Long pedidoId, Pageable pageable);

    List<ItemPedido> findByProdutoId(Long produtoId);

    List<ItemPedido> findByEstanteProdutoId(Long estanteProdutoId);

    Optional<ItemPedido> findByPedidoIdAndProdutoId(Long pedidoId, Long produtoId);

    // ===== BUSCAS COM JOIN FETCH =====

    @Query("SELECT i FROM ItemPedido i JOIN FETCH i.produto WHERE i.pedido.id = :pedidoId")
    List<ItemPedido> findByPedidoIdWithProduto(@Param("pedidoId") Long pedidoId);

    @Query("SELECT i FROM ItemPedido i JOIN FETCH i.pedido WHERE i.id = :id")
    Optional<ItemPedido> findByIdWithPedido(@Param("id") Long id);

    @Query("SELECT i FROM ItemPedido i " +
            "JOIN FETCH i.produto p " +
            "JOIN FETCH i.estanteProduto ep " +
            "WHERE i.pedido.id = :pedidoId")
    List<ItemPedido> findByPedidoIdCompleto(@Param("pedidoId") Long pedidoId);

    // ===== CONSULTAS AGREGADAS =====

    @Query("SELECT COUNT(i) FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    Integer countItensByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT SUM(i.quantidade) FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    BigDecimal sumQuantidadeByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT SUM(i.precoTotal) FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    Double sumPrecoTotalByPedidoId(@Param("pedidoId") Long pedidoId);

    // ===== MÉTRICAS POR PRODUTO =====

    @Query("SELECT i.produto.id, i.produto.nome, COUNT(i), SUM(i.quantidade), SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "GROUP BY i.produto.id, i.produto.nome " +
            "ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> getProdutosMaisVendidos();

    @Query("SELECT i.produto.id, i.produto.nome, SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "WHERE i.pedido.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY i.produto.id, i.produto.nome " +
            "ORDER BY SUM(i.precoTotal) DESC")
    List<Object[]> getProdutosMaisFaturamento(@Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim);

    @Query("SELECT i.produto.id, i.produto.nome, COUNT(DISTINCT i.pedido.id) " +
            "FROM ItemPedido i " +
            "GROUP BY i.produto.id, i.produto.nome " +
            "ORDER BY COUNT(DISTINCT i.pedido.id) DESC")
    List<Object[]> getProdutosMaisPedidos();

    // ===== MÉTRICAS POR ESTANTE =====

    @Query("SELECT i.estanteProduto.estante.id, i.estanteProduto.estante.periodo, " +
            "SUM(i.quantidade), SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "GROUP BY i.estanteProduto.estante.id")
    List<Object[]> getVendasPorEstante();

    @Query("SELECT i.estanteProduto.id, SUM(i.quantidade), SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "WHERE i.estanteProduto.estante.id = :estanteId " +
            "GROUP BY i.estanteProduto.id")
    List<Object[]> getVendasPorEstanteProduto(@Param("estanteId") Long estanteId);

    // ===== CONSULTAS POR PERÍODO =====

    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.dataCriacao BETWEEN :inicio AND :fim")
    List<ItemPedido> findByPeriodo(@Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim);

    @Query("SELECT i.produto.id, i.produto.nome, SUM(i.quantidade), SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "WHERE i.pedido.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY i.produto.id, i.produto.nome")
    List<Object[]> getVendasPorProdutoNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                @Param("fim") LocalDateTime fim);

    // ===== VERIFICAÇÕES =====

    boolean existsByPedidoIdAndProdutoId(Long pedidoId, Long produtoId);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM ItemPedido i WHERE i.pedido.id = :pedidoId AND i.produto.id = :produtoId")
    boolean isProdutoNoPedido(@Param("pedidoId") Long pedidoId,
                              @Param("produtoId") Long produtoId);

    // ===== OPERAÇÕES DE ATUALIZAÇÃO EM MASSA =====

    @Modifying
    @Query("UPDATE ItemPedido i SET i.observacao = CONCAT(i.observacao, ' - ', :observacao) " +
            "WHERE i.pedido.id = :pedidoId")
    int adicionarObservacaoEmTodosItens(@Param("pedidoId") Long pedidoId,
                                        @Param("observacao") String observacao);

    @Modifying
    @Query("DELETE FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    void deleteByPedidoId(@Param("pedidoId") Long pedidoId);

    // ===== RELATÓRIOS AVANÇADOS =====

    @Query("SELECT FUNCTION('DATE', i.pedido.dataCriacao) as data, " +
            "COUNT(DISTINCT i.pedido.id), SUM(i.quantidade), SUM(i.precoTotal) " +
            "FROM ItemPedido i " +
            "WHERE i.pedido.dataCriacao BETWEEN :inicio AND :fim " +
            "GROUP BY FUNCTION('DATE', i.pedido.dataCriacao) " +
            "ORDER BY data DESC")
    List<Object[]> getRelatorioDiarioVendas(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    @Query("SELECT i.produto.id, i.produto.nome, i.produto.unidadeMedida.sigla, " +
            "SUM(i.quantidade), SUM(i.precoTotal), AVG(i.precoTotal / i.quantidade) " +
            "FROM ItemPedido i " +
            "GROUP BY i.produto.id, i.produto.nome, i.produto.unidadeMedida.sigla")
    List<Object[]> getRelatorioProdutosCompletos();

    // ===== MÉTRICAS DE DESEMPENHO =====

    @Query("SELECT AVG(i.precoTotal / i.quantidade) FROM ItemPedido i")
    BigDecimal getTicketMedioPorItem();

    @Query("SELECT i.produto.id, i.produto.nome, AVG(i.precoTotal / i.quantidade) " +
            "FROM ItemPedido i " +
            "GROUP BY i.produto.id, i.produto.nome")
    List<Object[]> getPrecoMedioPorProduto();
}