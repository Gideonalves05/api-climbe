package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginFuncionario", description = "Dados de autenticação do funcionário da Climbe")
public record LoginFuncionarioDto(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Schema(description = "E-mail do funcionário", example = "analista@climbe.com.br")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha de acesso", example = "Senha@123")
        String senha
) {
}
