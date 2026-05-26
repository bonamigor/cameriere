package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCnpj(String cnpj);
}
