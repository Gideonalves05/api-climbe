package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO de entrada do outbox de notificações")
public record NotificacaoOutboxDto(
    @Schema(description = "ID da entrada do outbox")
    Long idOutbox,
    
    @Schema(description = "ID da notificação")
    Integer idNotificacao,
    
    @Schema(description = "Canal de entrega")
    CanalNotificacao canal,
    
    @Schema(description = "Destino (ID do usuário ou email)")
    String destino,
    
    @Schema(description = "Status de entrega")
    StatusEntregaNotificacao status,
    
    @Schema(description = "Número de tentativas")
    Integer tentativas,

    @Schema(description = "Número máximo de tentativas")
    Integer maxTentativas,
    
    @Schema(description = "Data/hora da próxima tentativa")
    LocalDateTime proximaTentativa,
    
    @Schema(description = "Data/hora da última tentativa")
    LocalDateTime ultimaTentativaEm,
    
    @Schema(description = "Último erro")
    String ultimoErro,
    
    @Schema(description = "Data/hora de criação")
    LocalDateTime criadoEm,
    
    @Schema(description = "Data/hora de atualização")
    LocalDateTime atualizadoEm
) {
}
