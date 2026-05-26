package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Estante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstanteRepository extends JpaRepository<Estante, Long> {

    // Buscas básicas
    Optional<Estante> findByPeriodoAndClienteId(String periodo, Long clienteId);

    List<Estante> findByClienteId(Long clienteId);

    Page<Estante> findByClienteIdAndAtivaTrue(Long clienteId, Pageable pageable);

    List<Estante> findByAtivaTrue();

    // Buscas por período
    @Query("SELECT e FROM Estante e WHERE e.periodo = :periodo AND e.ativa = true")
    List<Estante> findEstantesAtivasPorPeriodo(@Param("periodo") String periodo);

    @Query("SELECT e FROM Estante e WHERE e.periodo BETWEEN :inicio AND :fim")
    List<Estante> findEstantesPorPeriodo(@Param("inicio") String inicio,
                                         @Param("fim") String fim);

    // Consultas com agregações
    @Query("SELECT e.cliente.id, COUNT(e) FROM Estante e GROUP BY e.cliente.id")
    List<Object[]> contarEstantesPorCliente();

    @Query("SELECT e.periodo, COUNT(e) FROM Estante e WHERE e.ativa = true GROUP BY e.periodo")
    List<Object[]> contarEstantesAtivasPorPeriodo();

    // Busca com join fetch para evitar N+1
    @Query("SELECT DISTINCT e FROM Estante e LEFT JOIN FETCH e.produtos WHERE e.id = :id")
    Optional<Estante> findByIdWithProdutos(@Param("id") Long id);

    @Query("SELECT e FROM Estante e JOIN FETCH e.cliente WHERE e.id = :id")
    Optional<Estante> findByIdWithCliente(@Param("id") Long id);

    // Operações de atualização em lote
    @Modifying
    @Query("UPDATE Estante e SET e.ativa = false WHERE e.cliente.id = :clienteId")
    int desativarEstantesPorCliente(@Param("clienteId") Long clienteId);

    @Modifying
    @Query("UPDATE Estante e SET e.observacao = CONCAT(e.observacao, ' - ', :observacao) WHERE e.id = :estanteId")
    int adicionarObservacao(@Param("estanteId") Long estanteId,
                            @Param("observacao") String observacao);

    // Validações
    boolean existsByPeriodoAndClienteIdAndAtivaTrue(String periodo, Long clienteId);

    @Query("SELECT COUNT(e) > 0 FROM Estante e WHERE e.id = :estanteId AND e.ativa = true")
    boolean isEstanteAtiva(@Param("estanteId") Long estanteId);
}