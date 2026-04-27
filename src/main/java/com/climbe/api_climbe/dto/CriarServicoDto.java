package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarServico", description = "Dados para criação de um novo serviço no catálogo")
public record CriarServicoDto(
        @NotBlank
        @Size(min = 2, max = 255)
        @Schema(description = "Nome do serviço", example = "Consultoria Financeira")
        String nome
) {
}
