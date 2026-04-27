package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tarefas_contrato", indexes = {
    @Index(name = "idx_tarefas_contrato_contrato", columnList = "contrato_id"),
    @Index(name = "idx_tarefas_contrato_coluna", columnList = "coluna_id"),
    @Index(name = "idx_tarefas_contrato_responsavel", columnList = "responsavel_principal_id"),
    @Index(name = "idx_tarefas_contrato_data_limite", columnList = "data_limite")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarefa")
    @EqualsAndHashCode.Include
    private Integer idTarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NonNull
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coluna_id", nullable = false)
    @NonNull
    private ColunaKanban coluna;

    @Column(name = "titulo", nullable = false, length = 160)
    @NonNull
    private String titulo;

    @Lob
    @Column(name = "descricao")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade", nullable = false)
    @NonNull
    private PrioridadeTarefa prioridade = PrioridadeTarefa.MEDIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_principal_id", nullable = false)
    @NonNull
    private Usuario responsavelPrincipal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tarefa_co_responsaveis",
        joinColumns = @JoinColumn(name = "tarefa_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @NonNull
    private Set<Usuario> coResponsaveis = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tarefa_observadores",
        joinColumns = @JoinColumn(name = "tarefa_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @NonNull
    private Set<Usuario> observadores = new HashSet<>();

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_limite")
    private LocalDateTime dataLimite;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id", nullable = false)
    @NonNull
    private Usuario criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    @NonNull
    private Timestamp criadoEm;

    @Column(name = "atualizado_em")
    private Timestamp atualizadoEm;

    @OneToMany(mappedBy = "tarefa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    private Set<TarefaDependencia> dependencias = new HashSet<>();

    @OneToMany(mappedBy = "tarefa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    private Set<TarefaAnexo> anexos = new HashSet<>();

    @OneToMany(mappedBy = "tarefa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    private Set<TarefaComentario> comentarios = new HashSet<>();

    @OneToMany(mappedBy = "tarefa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    private Set<TarefaChecklistItem> checklistItens = new HashSet<>();

    @OneToMany(mappedBy = "tarefa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    private Set<TarefaLink> links = new HashSet<>();

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
