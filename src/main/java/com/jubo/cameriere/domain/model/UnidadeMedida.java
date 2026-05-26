package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "unidades_medida",
        uniqueConstraints = @UniqueConstraint(columnNames = "sigla"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMedida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sigla é obrigatória")
    @Size(max = 4, message = "Sigla não pode exceder 4 caracteres")
    private String sigla;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 50, message = "Descrição não pode exceder 50 caracteres")
    private String descricao;
}