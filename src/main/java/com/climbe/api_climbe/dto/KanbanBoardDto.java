package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "KanbanBoard", description = "DTO do board Kanban montado")
public record KanbanBoardDto(
    @Schema(description = "Lista de colunas com suas tarefas")
    List<ColunaComTarefasDto> colunas
) {
    @Schema(name = "ColunaComTarefas", description = "Coluna do Kanban com suas tarefas")
    public record ColunaComTarefasDto(
        @Schema(description = "Dados da coluna")
        ColunaDto coluna,
        
        @Schema(description = "Tarefas nesta coluna")
        List<TarefaDto> tarefas
    ) {}
}
