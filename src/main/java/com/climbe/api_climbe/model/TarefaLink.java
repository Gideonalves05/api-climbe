package com.climbe.api_climbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tarefa_links", indexes = {
    @Index(name = "idx_tarefa_links_tarefa", columnList = "tarefa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_link")
    @EqualsAndHashCode.Include
    private Integer idLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private TarefaContrato tarefa;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "titulo", length = 160)
    private String titulo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Timestamp criadoEm;

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) criadoEm = Timestamp.valueOf(java.time.LocalDateTime.now());
    }
}
