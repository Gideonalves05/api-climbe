package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.StatusContrato;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contratos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    @EqualsAndHashCode.Include
    private Integer idContrato;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id", unique = true)
    private Proposta proposta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private StatusContrato status;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Lob
    @Column(name = "arquivo_conteudo", columnDefinition = "LONGBLOB")
    private byte[] arquivoConteudo;

    @Column(name = "arquivo_nome", length = 255)
    private String arquivoNome;

    @Column(name = "arquivo_mime", length = 120)
    private String arquivoMime;

    @Column(name = "arquivo_tamanho")
    private Long arquivoTamanho;

    @OneToMany(mappedBy = "contrato")
    private Set<Planilha> planilhas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "contrato")
    private Set<Relatorio> relatorios = new LinkedHashSet<>();
}
