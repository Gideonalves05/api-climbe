package com.climbe.api_climbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "servicos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    @EqualsAndHashCode.Include
    private Integer idServico;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @ManyToMany(mappedBy = "servicos")
    private Set<Empresa> empresas = new LinkedHashSet<>();
}
