package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(name = "AtualizarTarefa", description = "DTO para atualização de tarefa de contrato")
public record AtualizarTarefaDto(
    @Size(max = 160, message = "Título deve ter no máximo 160 caracteres")
    @Schema(description = "Título da tarefa")
    String titulo,
    
    @Schema(description = "Descrição detalhada da tarefa")
    String descricao,
    
    @Schema(description = "Prioridade da tarefa")
    PrioridadeTarefa prioridade,
    
    @Schema(description = "ID do usuário responsável principal")
    Integer responsavelPrincipalId,
    
    @Future(message = "Data limite deve ser no futuro")
    @Schema(description = "Data limite para conclusão da tarefa")
    LocalDateTime dataLimite,
    
    @Schema(description = "ID da coluna do Kanban")
    Integer colunaId,
    
    @Schema(description = "IDs dos co-responsáveis")
    Set<Integer> coResponsaveisIds,
    
    @Schema(description = "IDs dos observadores")
    Set<Integer> observadoresIds,
    
    @Schema(description = "IDs das tarefas das quais esta tarefa depende")
    Set<Integer> dependenciasIds
) {}
