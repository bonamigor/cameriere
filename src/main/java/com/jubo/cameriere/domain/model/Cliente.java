package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.*;

@Entity
@Table(name = "clientes",
        uniqueConstraints = @UniqueConstraint(columnNames = "cnpj"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
            message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    @Column(nullable = false, length = 18)
    private String cnpj;

    @Embedded
    @Valid
    private Endereco endereco;

    @Email(message = "Email deve ser válido")
    @Size(max = 255, message = "Email não pode exceder 255 caracteres")
    @Column
    private String email;

    @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
    @Column
    private String telefone;

    @Column
    private Boolean ativo = true;
}
