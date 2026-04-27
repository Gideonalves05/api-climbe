package com.climbe.api_climbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tarefa_anexos", indexes = {
    @Index(name = "idx_tarefa_anexos_tarefa", columnList = "tarefa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TarefaAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anexo")
    @EqualsAndHashCode.Include
    private Integer idAnexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarefa_id", nullable = false)
    @NonNull
    private TarefaContrato tarefa;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    @NonNull
    private String nomeArquivo;

    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "drive_file_id", length = 120)
    private String driveFileId;

    @Column(name = "url_acesso", length = 500)
    private String urlAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upload_por_id", nullable = false)
    @NonNull
    private Usuario uploadPor;

    @Column(name = "upload_em", nullable = false, updatable = false)
    @NonNull
    private Timestamp uploadEm;

    @PrePersist
    protected void onCreate() {
        uploadEm = Timestamp.valueOf(java.time.LocalDateTime.now());
    }
}
