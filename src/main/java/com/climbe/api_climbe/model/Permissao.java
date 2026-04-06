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
@Table(name = "permissoes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permissao")
    @EqualsAndHashCode.Include
    private Integer idPermissao;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @ManyToMany(mappedBy = "permissoes")
    private Set<Usuario> usuarios = new LinkedHashSet<>();
}
