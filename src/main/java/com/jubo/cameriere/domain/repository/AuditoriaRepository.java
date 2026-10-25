package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.AcaoAuditoria;
import com.jubo.cameriere.domain.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    @Query("""
            SELECT a FROM Auditoria a
            WHERE (:acao IS NULL OR a.acao = :acao)
              AND (:email IS NULL OR LOWER(a.usuarioEmail) LIKE LOWER(CONCAT('%', :email, '%')))
            """)
    Page<Auditoria> buscar(@Param("acao") AcaoAuditoria acao,
                           @Param("email") String email,
                           Pageable pageable);

    @Modifying
    @Query("DELETE FROM Auditoria a WHERE a.dataHora < :limite")
    int deleteByDataHoraBefore(@Param("limite") LocalDateTime limite);
}
