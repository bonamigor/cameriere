package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "item_pedido")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedido extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "preco_total", nullable = false)
    private BigDecimal precoTotal;

    private String observacao;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "estante_id", nullable = false)
    private EstanteProduto estanteProduto;

    @Transient
    private BigDecimal precoUnitario;

    public BigDecimal getPrecoUnitario() {
        if (quantidade != null && quantidade.compareTo(BigDecimal.ZERO) > 0) {
            return precoTotal.divide(quantidade, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public void calcularPrecoTotal() {
        if (estanteProduto != null && quantidade != null) {
            this.precoTotal = estanteProduto.getPrecoVenda()
                    .multiply(quantidade);
        }
    }
}