package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCnpj(String cnpj);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.empresa")
    List<Cliente> findAllWithEmpresa();

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.empresa WHERE c.id = :id")
    Optional<Cliente> findByIdWithEmpresa(@Param("id") Long id);
}
