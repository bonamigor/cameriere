package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {
    boolean existsBySigla(String sigla);
    Optional<UnidadeMedida> findBySigla(String sigla);
}
