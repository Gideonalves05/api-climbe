package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoColuna;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarColuna", description = "DTO para criação de coluna do Kanban")
public record CriarColunaDto(
    @NotNull(message = "Nome é obrigatório")
    @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
    @Schema(description = "Nome da coluna", example = "Em Andamento")
    String nome,
    
    @NotNull(message = "Ordem é obrigatória")
    @Schema(description = "Posição da coluna no board", example = "2")
    Integer ordem,
    
    @NotNull(message = "Tipo é obrigatório")
    @Schema(description = "Tipo da coluna")
    TipoColuna tipo,
    
    @Size(max = 7, message = "Cor deve ter no máximo 7 caracteres")
    @Schema(description = "Cor em formato hexadecimal", example = "#3b82f6")
    String cor
) {}
