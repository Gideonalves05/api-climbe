package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DecisaoProposta", description = "Decisão sobre a proposta comercial e seleção do responsável pelo contrato")
public record DecisaoPropostaDto(
        @NotNull(message = "Decisão é obrigatória")
        @Schema(description = "Se a proposta foi aprovada", example = "true")
        Boolean aprovada,

        @Schema(description = "ID do funcionário responsável por criar o contrato (obrigatório quando aprovada=true)", example = "3")
        Integer idFuncionarioResponsavelContrato
) {
}
