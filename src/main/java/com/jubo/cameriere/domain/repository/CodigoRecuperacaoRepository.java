package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.CodigoRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodigoRecuperacaoRepository extends JpaRepository<CodigoRecuperacao, Long> {

    List<CodigoRecuperacao> findByUsuarioIdAndUsadoEmIsNull(Long usuarioId);

    long countByUsuarioIdAndUsadoEmIsNull(Long usuarioId);

    @Modifying
    @Query("DELETE FROM CodigoRecuperacao c WHERE c.usuarioId = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);
}
