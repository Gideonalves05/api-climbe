package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarChecklistItem", description = "DTO para criação de item de checklist em tarefa")
public record CriarChecklistItemDto(
    @NotBlank(message = "Descrição do item é obrigatória")
    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição do item de checklist", example = "Validar documento fiscal")
    String descricao,
    
    @Schema(description = "Ordem do item no checklist")
    Integer ordem
) {}
