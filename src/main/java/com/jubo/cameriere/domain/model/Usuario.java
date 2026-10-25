package com.jubo.cameriere.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome não pode exceder 255 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 255, message = "Email não pode exceder 255 caracteres")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @NotNull(message = "Admin é obrigatório")
    @Column(name = "admin")
    private Boolean admin = false;

    @OneToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Segredo TOTP criptografado; preenchido ao iniciar a configuração do 2FA. */
    @Column(name = "totp_segredo")
    private String totpSegredo;

    @Column(name = "totp_ativo", nullable = false)
    private boolean totpAtivo = false;

    /** Último passo de 30s aceito, para impedir a reutilização do mesmo código. */
    @Column(name = "totp_ultimo_passo")
    private Long totpUltimoPasso;

    public Usuario(Long id, String nome, String email, String senha, Boolean admin, Cliente cliente) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.admin = admin;
        this.cliente = cliente;
    }
}