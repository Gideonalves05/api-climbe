package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.StatusProposta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "propostas")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposta")
    @EqualsAndHashCode.Include
    private Integer idProposta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusProposta status;

    @Column(name = "documento_proposta", columnDefinition = "TEXT")
    private String documentoProposta;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Lob
    @Column(name = "arquivo_conteudo", columnDefinition = "LONGBLOB")
    private byte[] arquivoConteudo;

    @Column(name = "arquivo_nome", length = 255)
    private String arquivoNome;

    @Column(name = "arquivo_mime", length = 120)
    private String arquivoMime;

    @Column(name = "arquivo_tamanho")
    private Long arquivoTamanho;

    @OneToOne(mappedBy = "proposta")
    private Contrato contrato;
}
