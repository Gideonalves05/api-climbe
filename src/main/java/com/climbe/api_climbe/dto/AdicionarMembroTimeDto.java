package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.PapelTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdicionarMembroTime", description = "DTO para adicionar membro ao time do contrato")
public record AdicionarMembroTimeDto(
    @NotNull(message = "ID do usuário é obrigatório")
    @Schema(description = "ID do usuário a ser adicionado ao time")
    Integer usuarioId,
    
    @Schema(description = "Papel do membro no time")
    PapelTime papel
) {}
