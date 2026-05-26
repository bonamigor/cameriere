package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
    private String nome;

    @NotNull(message = "Preço de custo é obrigatório")
    @Positive(message = "Preço de custo deve ser maior que zero")
    @Column(name = "preco_custo")
    private Double precoCusto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_medida", referencedColumnName = "sigla")
    @NotNull(message = "Unidade de medida é obrigatória")
    private UnidadeMedida unidadeMedida;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @OneToMany(mappedBy = "produto")
    private List<EstanteProduto> estantes = new ArrayList<>();
}