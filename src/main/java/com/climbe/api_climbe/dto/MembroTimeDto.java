package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.PapelTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MembroTime", description = "DTO de membro do time do contrato")
public record MembroTimeDto(
    @Schema(description = "ID do membro do time")
    Integer idMembroTime,
    
    @Schema(description = "ID do contrato")
    Integer contratoId,
    
    @Schema(description = "ID do usuário")
    Integer usuarioId,
    
    @Schema(description = "Nome do usuário")
    String usuarioNome,
    
    @Schema(description = "Email do usuário")
    String usuarioEmail,
    
    @Schema(description = "Papel do membro no time")
    PapelTime papel,
    
    @Schema(description = "Data de entrada no time")
    java.time.LocalDate dataEntrada,
    
    @Schema(description = "Indica se o membro está ativo")
    Boolean ativo,
    
    @Schema(description = "Data de criação")
    java.time.LocalDateTime criadoEm,
    
    @Schema(description = "Data de atualização")
    java.time.LocalDateTime atualizadoEm
) {}
