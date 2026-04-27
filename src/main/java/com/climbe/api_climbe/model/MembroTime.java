package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.PapelTime;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.sql.Timestamp;

@Entity
@Table(name = "membros_time", indexes = {
    @Index(name = "idx_membros_time_contrato", columnList = "contrato_id"),
    @Index(name = "idx_membros_time_usuario", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE membros_time SET ativo = false WHERE id_membro_time = ?")
public class MembroTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membro_time")
    @EqualsAndHashCode.Include
    private Integer idMembroTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @NonNull
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NonNull
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false)
    @NonNull
    private PapelTime papel = PapelTime.MEMBRO;

    @Column(name = "data_entrada", nullable = false)
    @NonNull
    private LocalDate dataEntrada = LocalDate.now();

    @Column(name = "ativo", nullable = false)
    @NonNull
    private Boolean ativo = true;

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
