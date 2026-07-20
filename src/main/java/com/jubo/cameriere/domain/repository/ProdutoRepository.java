package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p JOIN FETCH p.unidadeMedida WHERE p.id = :id")
    Optional<Produto> findByIdWithUnidadeMedida(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Produto p JOIN FETCH p.unidadeMedida")
    List<Produto> findAllWithUnidadeMedida();
}
