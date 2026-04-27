package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoColuna;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ColunaKanban", description = "DTO de coluna do Kanban")
public record ColunaDto(
    @Schema(description = "ID da coluna")
    Integer idColuna,
    
    @Schema(description = "ID do contrato")
    Integer contratoId,
    
    @Schema(description = "Nome da coluna")
    String nome,
    
    @Schema(description = "Posição da coluna no board")
    Integer ordem,
    
    @Schema(description = "Tipo da coluna")
    TipoColuna tipo,
    
    @Schema(description = "Cor em formato hexadecimal")
    String cor,
    
    @Schema(description = "Data de criação")
    java.time.LocalDateTime criadoEm,
    
    @Schema(description = "Data de atualização")
    java.time.LocalDateTime atualizadoEm
) {}
