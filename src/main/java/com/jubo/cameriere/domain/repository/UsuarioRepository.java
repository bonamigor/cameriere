package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cliente WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cliente WHERE u.id = :id")
    Optional<Usuario> findByIdWithCliente(@Param("id") Long id);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.cliente WHERE u.cliente.id = :clienteId ORDER BY u.id ASC")
    List<Usuario> findAllByClienteIdWithCliente(@Param("clienteId") Long clienteId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByClienteIdAndIdNot(Long clienteId, Long id);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.cliente")
    List<Usuario> findAllWithCliente();
}
