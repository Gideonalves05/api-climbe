package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AtivarConta", description = "Dados para ativação de conta com token")
public record AtivarContaDto(
        @NotBlank(message = "Token de ativação é obrigatório")
        @Schema(description = "Token de ativação recebido por e-mail", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        String token,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        @Schema(description = "Nova senha da conta", example = "Senha@123")
        String senha,

        @NotBlank(message = "Confirmação de senha é obrigatória")
        @Schema(description = "Confirmação da nova senha", example = "Senha@123")
        String confirmacaoSenha
) {
}
