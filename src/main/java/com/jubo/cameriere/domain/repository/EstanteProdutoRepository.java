package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.EstanteProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstanteProdutoRepository extends JpaRepository<EstanteProduto, Long> {

    // Buscas básicas
    Optional<EstanteProduto> findByEstanteIdAndProdutoId(Long estanteId, Long produtoId);

    List<EstanteProduto> findByEstanteId(Long estanteId);

    List<EstanteProduto> findByProdutoId(Long produtoId);

    Page<EstanteProduto> findByEstanteIdAndAtivoTrue(Long estanteId, Pageable pageable);

    // Buscas com condições
    @Query("SELECT ep FROM EstanteProduto ep WHERE ep.estante.id = :estanteId AND ep.quantidade > 0 AND ep.ativo = true")
    List<EstanteProduto> findProdutosDisponiveisNaEstante(@Param("estanteId") Long estanteId);

    @Modifying
    @Query("UPDATE EstanteProduto ep SET ep.ativo = false WHERE ep.estante.id = :estanteId")
    int desativarTodosProdutosDaEstante(@Param("estanteId") Long estanteId);

    // Verificações
    boolean existsByEstanteIdAndProdutoId(Long estanteId, Long produtoId);

    @Query("SELECT COUNT(ep) > 0 FROM EstanteProduto ep WHERE ep.estante.id = :estanteId AND ep.produto.id = :produtoId AND ep.ativo = true")
    boolean isProdutoAtivoNaEstante(@Param("estanteId") Long estanteId, @Param("produtoId") Long produtoId);
}