package com.climbe.api_climbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "relatorios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorio")
    @EqualsAndHashCode.Include
    private Integer idRelatorio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "url_pdf", length = 255)
    private String urlPdf;

    @Column(name = "data_envio")
    private LocalDate dataEnvio;
}
