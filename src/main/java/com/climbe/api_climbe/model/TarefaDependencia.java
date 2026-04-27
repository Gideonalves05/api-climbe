package com.climbe.api_climbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tarefa_dependencias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tarefa_id", "depende_de_id"}, name = "uk_tarefa_dependencias")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaDependencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dependencia")
    @EqualsAndHashCode.Include
    private Integer idDependencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    @NonNull
    private TarefaContrato tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depende_de_id", nullable = false)
    @NonNull
    private TarefaContrato dependeDe;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @NonNull
    private Timestamp criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = Timestamp.valueOf(java.time.LocalDateTime.now());
    }
}
