package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
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
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notificacoes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    @EqualsAndHashCode.Include
    private Integer idNotificacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "mensagem", nullable = false, length = 255)
    private String mensagem;

    @Column(name = "data_envio")
    private LocalDate dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 40)
    private TipoNotificacao tipo;
}
