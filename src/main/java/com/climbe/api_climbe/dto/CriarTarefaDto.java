package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(name = "CriarTarefa", description = "DTO para criação de tarefa de contrato")
public record CriarTarefaDto(
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 160, message = "Título deve ter no máximo 160 caracteres")
    @Schema(description = "Título da tarefa", example = "Validar documento fiscal")
    String titulo,

    @Schema(description = "Descrição detalhada da tarefa")
    String descricao,

    @Schema(description = "Prioridade da tarefa", example = "MEDIA")
    PrioridadeTarefa prioridade,

    @Schema(description = "ID do usuário responsável principal (primeiro da lista de responsáveis)")
    Integer responsavelPrincipalId,

    @Schema(description = "Data de início prevista")
    LocalDateTime dataInicio,

    @Schema(description = "Data limite para conclusão da tarefa")
    LocalDateTime dataLimite,

    @Schema(description = "IDs dos co-responsáveis (outros responsáveis além do principal)")
    Set<Integer> coResponsaveisIds,

    @Schema(description = "IDs dos observadores")
    Set<Integer> observadoresIds,

    @Schema(description = "IDs das tarefas das quais esta tarefa depende")
    Set<Integer> dependenciasIds,

    @Schema(description = "Subtarefas (itens de checklist); concluir todos conclui a tarefa pai")
    List<String> subtarefas,

    @Schema(description = "Links relacionados à tarefa")
    List<CriarTarefaLinkDto> links
) {}
