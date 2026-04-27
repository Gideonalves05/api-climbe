package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de notificação com payload completo para consumo real-time e listagens.
 */
@Schema(description = "DTO de notificação")
public record NotificacaoDto(
        @Schema(description = "ID da notificação")
        Integer idNotificacao,
        @Schema(description = "ID do usuário destinatário")
        Integer idUsuario,
        @Schema(description = "Tipo da notificação")
        TipoNotificacao tipo,
        @Schema(description = "Título")
        String titulo,
        @Schema(description = "Mensagem")
        String mensagem,
        @Schema(description = "Link de destino para ação")
        String linkDestino,
        @Schema(description = "Payload adicional em JSON")
        String payload,
        @Schema(description = "Indica se a notificação foi lida")
        Boolean lida,
        @Schema(description = "Data/hora de marcação como lida")
        LocalDateTime lidaEm,
        @Schema(description = "Data/hora de criação")
        LocalDateTime criadoEm
) {
}
