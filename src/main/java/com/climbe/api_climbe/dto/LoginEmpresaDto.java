package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginEmpresa", description = "Dados de autenticação da empresa")
public record LoginEmpresaDto(
        @NotBlank(message = "Informe e-mail ou CNPJ")
        @Schema(description = "Login da empresa (e-mail ou CNPJ)", example = "12.345.678/0001-90")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha de acesso", example = "Empresa@123")
        String senha
) {
}
