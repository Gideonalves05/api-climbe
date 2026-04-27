package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@Table(name = "usuarios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @EqualsAndHashCode.Include
    private Integer idUsuario;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "contato", length = 255)
    private String contato;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoUsuario situacao;

    @Column(name = "senha_hash", nullable = false, length = 60)
    private String senhaHash;

    @Column(name = "token_ativacao", length = 64)
    private String tokenAtivacao;

    @Column(name = "token_expira_em")
    private java.time.LocalDateTime tokenExpiraEm;

    @Column(name = "motivo_rejeicao", length = 500)
    private String motivoRejeicao;

    @Column(name = "criado_em")
    private java.time.LocalDateTime criadoEm;

    @OneToMany(mappedBy = "usuario")
    private Set<Notificacao> notificacoes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "usuarioResponsavel")
    private Set<Proposta> propostasResponsavel = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_permissoes",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_permissao")
    )
    private Set<Permissao> permissoes = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "participantes")
    private Set<Reuniao> reunioes = new LinkedHashSet<>();
}
