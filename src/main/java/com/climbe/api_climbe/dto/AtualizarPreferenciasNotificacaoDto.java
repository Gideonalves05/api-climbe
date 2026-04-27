package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "DTO para atualização de preferências em lote")
public record AtualizarPreferenciasNotificacaoDto(
    @NotNull
    @Schema(description = "Lista de preferências a atualizar/criar")
    List<PreferenciaNotificacaoDto> preferencias
) {
}
