package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoColuna;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "AtualizarColuna", description = "DTO para atualização de coluna do Kanban")
public record AtualizarColunaDto(
    @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
    @Schema(description = "Nome da coluna")
    String nome,
    
    @Schema(description = "Posição da coluna no board")
    Integer ordem,
    
    @Schema(description = "Tipo da coluna")
    TipoColuna tipo,
    
    @Size(max = 7, message = "Cor deve ter no máximo 7 caracteres")
    @Schema(description = "Cor em formato hexadecimal")
    String cor
) {}
