package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Filtros para consulta de eventos de auditoria")
public record FiltroAuditoriaDto(
        @Schema(description = "Tipo de evento")
        TipoEventoAuditoria tipoEvento,
        
        @Schema(description = "Entidade (ex: CONTRATO, TAREFA, USUARIO)")
        String entidade,
        
        @Schema(description = "ID da entidade")
        Integer entidadeId,
        
        @Schema(description = "ID do usuário ator")
        Integer atorUsuarioId,
        
        @Schema(description = "Data de início do período")
        LocalDateTime dataInicio,
        
        @Schema(description = "Data de fim do período")
        LocalDateTime dataFim,
        
        @Schema(description = "Número da página (0-indexed)", example = "0")
        Integer pagina,
        
        @Schema(description = "Tamanho da página", example = "20")
        Integer tamanho
) {
}
