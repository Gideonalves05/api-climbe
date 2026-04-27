package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RejeitarUsuario", description = "Dados para rejeição de usuário pendente")
public record RejeitarUsuarioDto(
        @NotBlank(message = "Motivo da rejeição é obrigatório")
        @Size(max = 500, message = "Motivo da rejeição deve ter no máximo 500 caracteres")
        @Schema(description = "Motivo pelo qual a solicitação foi rejeitada", example = "O cargo solicitado não está disponível no momento")
        String motivo
) {
}
