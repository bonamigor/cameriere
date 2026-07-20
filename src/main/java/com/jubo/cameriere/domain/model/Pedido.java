package com.jubo.cameriere.domain.model;
import com.jubo.cameriere.domain.exception.PedidoStatusInvalidoException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @Column(name = "data_previsao_entrega")
    private LocalDate dataPrevisaoEntrega;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    private Boolean entregue;

    private String observacao;

    @Column(name = "obs_cancelamento")
    private String obsCancelamento;

    @Column(name = "is_finalizado")
    private Boolean finalizado;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_nota_id")
    private Empresa empresaNota;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();


    public void recalcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemPedido::getPrecoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirmar(LocalDate dataPrevisaoEntrega) {
        if (this.status != StatusPedido.PENDENTE) {
            throw new PedidoStatusInvalidoException("Pedido não pode ser confirmado no status: " + this.status);
        }
        if (dataPrevisaoEntrega == null) {
            throw new PedidoStatusInvalidoException("Data prevista de entrega é obrigatória para confirmar o pedido");
        }
        this.status = StatusPedido.CONFIRMADO;
        this.dataConfirmacao = LocalDateTime.now();
        this.dataPrevisaoEntrega = dataPrevisaoEntrega;
    }

    public void cancelar(String motivo) {
        if (this.status == StatusPedido.ENTREGUE) {
            throw new PedidoStatusInvalidoException("Pedidos entregues não podem ser cancelados");
        }

        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Pedido já está cancelado");
        }

        this.status = StatusPedido.CANCELADO;
        this.dataCancelamento = LocalDateTime.now();
        this.obsCancelamento = motivo;
    }

    public void entregar() {
        if (this.status != StatusPedido.CONFIRMADO) {
            throw new PedidoStatusInvalidoException("Apenas pedidos confirmados podem ser entregues");
        }
        this.status = StatusPedido.ENTREGUE;
        this.dataEntrega = LocalDateTime.now();
        this.entregue = true;
        this.finalizado = true;
    }
}