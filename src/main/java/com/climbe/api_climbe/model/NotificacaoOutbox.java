package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
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
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notificacoes_outbox")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificacaoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_outbox")
    @EqualsAndHashCode.Include
    private Long idOutbox;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_notificacao", nullable = false)
    private Notificacao notificacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private CanalNotificacao canal;

    @Column(name = "destino", nullable = false, length = 255)
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEntregaNotificacao status = StatusEntregaNotificacao.PENDENTE;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "max_tentativas", nullable = false)
    private Integer maxTentativas = 6;

    @Column(name = "proxima_tentativa", nullable = false)
    private LocalDateTime proximaTentativa = LocalDateTime.now();

    @Column(name = "ultima_tentativa_em")
    private LocalDateTime ultimaTentativaEm;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
