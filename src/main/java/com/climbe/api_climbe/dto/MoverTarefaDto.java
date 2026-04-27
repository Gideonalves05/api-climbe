package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MoverTarefa", description = "Payload para mover um cartão do Kanban para outra coluna")
public record MoverTarefaDto(
        @NotNull
        @Schema(description = "ID da coluna destino", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer colunaDestinoId
) {
}
