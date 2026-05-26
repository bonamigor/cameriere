package com.jubo.cameriere.domain.repository;

import com.jubo.cameriere.domain.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>  {
}
