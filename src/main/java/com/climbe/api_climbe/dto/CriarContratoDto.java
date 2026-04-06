package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "CriarContrato", description = "Dados para criação de contrato a partir de proposta aprovada")
public record CriarContratoDto(
        @NotNull(message = "Proposta é obrigatória")
        @Schema(description = "ID da proposta aprovada", example = "15")
        Integer idProposta,

        @NotNull(message = "Data de início é obrigatória")
        @Schema(description = "Data de início do contrato", example = "2026-03-24")
        LocalDate dataInicio,

        @Schema(description = "Data fim prevista do contrato", example = "2027-03-24")
        LocalDate dataFim
) {
}
