package com.jubo.cameriere.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Código de uso único para entrar quando o usuário perde o aplicativo autenticador. */
@Entity
@Table(name = "usuario_codigos_recuperacao")
@Getter
@Setter
@NoArgsConstructor
public class CodigoRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "codigo_hash", nullable = false)
    private String codigoHash;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    public CodigoRecuperacao(Long usuarioId, String codigoHash) {
        this.usuarioId = usuarioId;
        this.codigoHash = codigoHash;
    }
}
