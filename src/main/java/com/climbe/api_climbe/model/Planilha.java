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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "planilhas")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Planilha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_planilha")
    @EqualsAndHashCode.Include
    private Integer idPlanilha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "url_google_sheets", length = 255)
    private String urlGoogleSheets;

    @Column(name = "bloqueada")
    private Boolean bloqueada;

    @Column(name = "permissao_visualizacao", length = 255)
    private String permissaoVisualizacao;
}
