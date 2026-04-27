package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AtualizarServico", description = "Dados para atualização de um serviço existente")
public record AtualizarServicoDto(
        @NotBlank
        @Size(min = 2, max = 255)
        @Schema(description = "Nome do serviço", example = "Consultoria Financeira Premium")
        String nome
) {
}
