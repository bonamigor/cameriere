package com.jubo.cameriere.domain.model;


import com.jubo.cameriere.domain.exception.EstoqueInsuficienteException;
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
        return Boolean.TRUE.equals(ativo) && quantidade != null && quantidade > 0;
    }

    public boolean possuiEstoqueSuficiente(BigDecimal quantidadeSolicitada) {
        if (quantidadeSolicitada == null || quantidade == null) {
            return false;
        }
        return BigDecimal.valueOf(quantidade).compareTo(quantidadeSolicitada) >= 0;
    }

    public void baixarEstoque(BigDecimal quantidadeSolicitada) {
        if (!possuiEstoqueSuficiente(quantidadeSolicitada)) {
            throw new EstoqueInsuficienteException(
                    String.format("Estoque insuficiente para o produto %s. Disponível: %d",
                            produto != null ? produto.getNome() : id, quantidade));
        }
        this.quantidade = this.quantidade - quantidadeSolicitada.intValue();
    }

    public void reporEstoque(BigDecimal quantidadeReposta) {
        if (quantidadeReposta == null) {
            return;
        }
        this.quantidade = this.quantidade + quantidadeReposta.intValue();
    }

    public BigDecimal calcularValorTotal(Integer quantidade) {
        return precoVenda.multiply(BigDecimal.valueOf(quantidade));
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        if (this.quantidade != null && this.quantidade > 0) {
            throw new IllegalStateException("Não é possível desativar produto com estoque positivo");
        }
        this.ativo = false;
    }
}
