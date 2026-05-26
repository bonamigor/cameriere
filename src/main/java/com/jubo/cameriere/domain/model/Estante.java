package com.jubo.cameriere.domain.model;


import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String periodo;

    private String observacao;

    @Column(nullable = false)
    private Boolean ativa;

    private Integer empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "estante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstanteProduto> produtos = new ArrayList<>();

    public void adicionarProduto(EstanteProduto produto) {
        produtos.add(produto);
        produto.setEstante(this);
    }

    public void removerProduto(EstanteProduto produto) {
        produtos.remove(produto);
        produto.setEstante(null);
    }
}
