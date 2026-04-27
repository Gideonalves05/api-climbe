package com.climbe.api_climbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tarefa_comentarios", indexes = {
    @Index(name = "idx_tarefa_comentarios_tarefa", columnList = "tarefa_id"),
    @Index(name = "idx_tarefa_comentarios_criado_em", columnList = "criado_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaComentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    @EqualsAndHashCode.Include
    private Integer idComentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    @NonNull
    private TarefaContrato tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    @NonNull
    private Usuario autor;

    @Lob
    @Column(name = "texto", nullable = false)
    @NonNull
    private String texto;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @NonNull
    private Timestamp criadoEm;

    @Column(name = "editado_em")
    private Timestamp editadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = Timestamp.valueOf(java.time.LocalDateTime.now());
    }
}
