package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.EstanteProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstanteProdutoRepository extends JpaRepository<EstanteProduto, Long> {

    @Query("""
            SELECT ep FROM EstanteProduto ep
            JOIN FETCH ep.estante
            JOIN FETCH ep.produto
            WHERE ep.estante.id = :estanteId AND ep.produto.id = :produtoId
            """)
    Optional<EstanteProduto> findByEstanteIdAndProdutoId(@Param("estanteId") Long estanteId,
                                                         @Param("produtoId") Long produtoId);

    @Query("""
            SELECT ep FROM EstanteProduto ep
            JOIN FETCH ep.estante
            JOIN FETCH ep.produto
            WHERE ep.id = :id
            """)
    Optional<EstanteProduto> findByIdWithAssociacoes(@Param("id") Long id);

    @Query(
            value = """
                    SELECT ep FROM EstanteProduto ep
                    JOIN FETCH ep.estante
                    JOIN FETCH ep.produto p
                    WHERE ep.estante.id = :estanteId AND ep.ativo = true
                    """,
            countQuery = """
                    SELECT COUNT(ep) FROM EstanteProduto ep
                    WHERE ep.estante.id = :estanteId AND ep.ativo = true
                    """
    )
    Page<EstanteProduto> findByEstanteIdAndAtivoTrue(@Param("estanteId") Long estanteId, Pageable pageable);

    @Query("""
            SELECT ep FROM EstanteProduto ep
            JOIN FETCH ep.estante
            JOIN FETCH ep.produto p
            LEFT JOIN FETCH p.unidadeMedida
            WHERE ep.estante.id = :estanteId AND ep.quantidade > 0 AND ep.ativo = true
            ORDER BY p.nome
            """)
    List<EstanteProduto> findProdutosDisponiveisNaEstante(@Param("estanteId") Long estanteId);

    @Modifying
    @Query("UPDATE EstanteProduto ep SET ep.ativo = false WHERE ep.estante.id = :estanteId")
    int desativarTodosProdutosDaEstante(@Param("estanteId") Long estanteId);

    boolean existsByEstanteIdAndProdutoId(Long estanteId, Long produtoId);
}
