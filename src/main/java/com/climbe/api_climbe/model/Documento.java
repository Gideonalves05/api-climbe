package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.StatusValidacaoDocumento;
import com.climbe.api_climbe.model.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "documentos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    @EqualsAndHashCode.Include
    private Integer idDocumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 60)
    private TipoDocumento tipoDocumento;

    @Column(name = "url", length = 255)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "validado", length = 30)
    private StatusValidacaoDocumento validado;
}
