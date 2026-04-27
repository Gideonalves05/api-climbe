package com.climbe.api_climbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tarefa_checklist_itens", indexes = {
    @Index(name = "idx_tarefa_checklist_itens_tarefa", columnList = "tarefa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    @EqualsAndHashCode.Include
    private Integer idItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    @NonNull
    private TarefaContrato tarefa;

    @Column(name = "descricao", nullable = false, length = 255)
    @NonNull
    private String descricao;

    @Column(name = "concluido", nullable = false)
    @NonNull
    private Boolean concluido = false;

    @Column(name = "ordem", nullable = false)
    @NonNull
    private Integer ordem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @NonNull
    private Timestamp criadoEm;

    @Column(name = "atualizado_em")
    private Timestamp atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = Timestamp.valueOf(java.time.LocalDateTime.now());
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = Timestamp.valueOf(java.time.LocalDateTime.now());
    }
}
