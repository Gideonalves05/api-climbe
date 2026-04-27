package com.climbe.api_climbe.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "proposta_arquivo")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PropostaArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_arquivo")
    @EqualsAndHashCode.Include
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposta_id", nullable = false, unique = true)
    private Proposta proposta;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "conteudo", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] conteudo;

    @CreationTimestamp
    @Column(name = "data_upload", nullable = false, updatable = false)
    private LocalDateTime dataUpload;
}
