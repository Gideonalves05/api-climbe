package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.StatusReuniao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reunioes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reuniao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reuniao")
    @EqualsAndHashCode.Include
    private Integer idReuniao;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "hora")
    private LocalTime hora;

    @Column(name = "presencial")
    private Boolean presencial;

    @Column(name = "local", length = 255)
    private String local;

    @Column(name = "pauta", columnDefinition = "TEXT")
    private String pauta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private StatusReuniao status;

    @ManyToMany
    @JoinTable(
            name = "participantes_reuniao",
            joinColumns = @JoinColumn(name = "id_reuniao"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<Usuario> participantes = new LinkedHashSet<>();
}
