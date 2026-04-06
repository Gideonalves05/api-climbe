package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResposta", description = "Resposta padrão de autenticação com token JWT")
public record TokenRespostaDto(
        @Schema(description = "Token JWT de acesso", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de expiração em segundos", example = "3600")
        Long expiresIn,

        @Schema(description = "Tipo da conta autenticada", example = "FUNCIONARIO")
        String tipoConta,

        @Schema(description = "Identificador da conta", example = "1")
        String identificador,

        @Schema(description = "Nome vinculado à conta", example = "Ana Souza")
        String nome,

        @Schema(description = "Cargo do usuário ou perfil da conta", example = "ANALISTA")
        String cargo
) {
}
