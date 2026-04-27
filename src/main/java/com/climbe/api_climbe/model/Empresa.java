package com.climbe.api_climbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "empresas")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    @EqualsAndHashCode.Include
    private Integer idEmpresa;

    @Column(name = "razao_social", length = 255)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false, length = 255)
    private String nomeFantasia;

    @Column(name = "cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "logradouro", length = 255)
    private String logradouro;

    @Column(name = "numero", length = 255)
    private String numero;

    @Column(name = "bairro", length = 255)
    private String bairro;

    @Column(name = "cidade", length = 255)
    private String cidade;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "cep", length = 9)
    private String cep;

    @Column(name = "telefone", length = 50)
    private String telefone;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "representante_nome", length = 255)
    private String representanteNome;

    @Column(name = "representante_cpf", length = 14)
    private String representanteCpf;

    @Column(name = "representante_contato", length = 50)
    private String representanteContato;

    @Column(name = "representante_email", length = 255)
    private String representanteEmail;

    @OneToMany(mappedBy = "empresa")
    private Set<Proposta> propostas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "empresa")
    private Set<Reuniao> reunioes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "empresa")
    private Set<Documento> documentos = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "empresa_servico",
            joinColumns = @JoinColumn(name = "id_empresa"),
            inverseJoinColumns = @JoinColumn(name = "id_servico")
    )
    private Set<Servico> servicos = new LinkedHashSet<>();
}
