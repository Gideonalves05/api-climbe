package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChecklistItem", description = "DTO de item de checklist")
public record ChecklistItemDto(
    @Schema(description = "ID do item")
    Integer idItem,
    
    @Schema(description = "ID da tarefa")
    Integer tarefaId,
    
    @Schema(description = "Descrição do item")
    String descricao,
    
    @Schema(description = "Indica se o item está concluído")
    Boolean concluido,
    
    @Schema(description = "Ordem do item no checklist")
    Integer ordem,
    
    @Schema(description = "Data de criação")
    java.time.LocalDateTime criadoEm,
    
    @Schema(description = "Data de atualização")
    java.time.LocalDateTime atualizadoEm
) {}
