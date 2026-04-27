package com.climbe.api_climbe.model;

import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "auditoria_eventos", indexes = {
    @Index(name = "idx_auditoria_tipo_evento", columnList = "tipo_evento"),
    @Index(name = "idx_auditoria_criado_em", columnList = "criado_em"),
    @Index(name = "idx_auditoria_ator_usuario_id", columnList = "ator_usuario_id"),
    @Index(name = "idx_auditoria_entidade", columnList = "entidade, entidade_id")
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEventoAuditoria tipoEvento;

    @Column(name = "entidade", nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id")
    private Integer entidadeId;

    @Column(name = "ator_usuario_id")
    private Integer atorUsuarioId;

    @Column(name = "ator_email", length = 255)
    private String atorEmail;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}
