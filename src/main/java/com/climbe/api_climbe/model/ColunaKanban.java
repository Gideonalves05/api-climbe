package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.TipoColuna;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "colunas_kanban", indexes = {
    @Index(name = "idx_colunas_kanban_contrato", columnList = "contrato_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ColunaKanban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coluna")
    @EqualsAndHashCode.Include
    private Integer idColuna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NonNull
    private Contrato contrato;

    @Column(name = "nome", nullable = false, length = 60)
    @NonNull
    private String nome;

    @Column(name = "ordem", nullable = false)
    @NonNull
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @NonNull
    private TipoColuna tipo;

    @Column(name = "cor", length = 7)
    private String cor;

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
