package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Representação de um evento de auditoria")
public record AuditoriaEventoDto(
        @Schema(description = "ID do evento")
        Integer id,
        
        @Schema(description = "Tipo do evento")
        TipoEventoAuditoria tipoEvento,
        
        @Schema(description = "Entidade afetada (ex: CONTRATO, TAREFA, USUARIO)")
        String entidade,
        
        @Schema(description = "ID da entidade afetada")
        Integer entidadeId,
        
        @Schema(description = "ID do usuário que realizou a ação")
        Integer atorUsuarioId,
        
        @Schema(description = "E-mail do usuário que realizou a ação")
        String atorEmail,
        
        @Schema(description = "ID de correlação para rastreamento distribuído")
        String correlationId,
        
        @Schema(description = "Payload JSON com detalhes do evento")
        String payloadJson,
        
        @Schema(description = "Data e hora de criação do evento")
        LocalDateTime criadoEm
) {
}
