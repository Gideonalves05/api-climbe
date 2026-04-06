package com.climbe.api_climbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cargos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    @EqualsAndHashCode.Include
    private Integer idCargo;

    @Column(name = "nome_cargo", nullable = false, length = 255)
    private String nomeCargo;

    @OneToMany(mappedBy = "cargo")
    private Set<Usuario> usuarios = new LinkedHashSet<>();
}
