package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de preferência de notificação")
public record PreferenciaNotificacaoDto(
    @Schema(description = "Tipo de notificação")
    TipoNotificacao tipo,
    @Schema(description = "Canal de entrega")
    CanalNotificacao canal,
    @Schema(description = "Indica se o canal está habilitado para este tipo")
    Boolean habilitado
) {
}
