package com.jubo.cameriere.domain.model;


import com.jubo.cameriere.api.exceptions.EstoqueInsuficienteException;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estante_produto",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_estante_produto",
                        columnNames = {"estante_id", "produto_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstanteProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estante_id", nullable = false)
    private Estante estante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (ativo == null) ativo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public boolean isDisponivel() {
        return ativo && quantidade > 0;
    }

    public BigDecimal calcularValorTotal(Integer quantidade) {
        return precoVenda.multiply(BigDecimal.valueOf(quantidade));
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        if (this.quantidade > 0) {
            throw new IllegalStateException("Não é possível desativar produto com estoque positivo");
        }
        this.ativo = false;
    }
}
