package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "Tarefa", description = "DTO de tarefa de contrato")
public record TarefaDto(
        Integer idTarefa,
        Integer contratoId,
        Integer idColuna,
        String colunaNome,
        String colunaTipo,
        String titulo,
        String descricao,
        PrioridadeTarefa prioridade,
        Integer idResponsavel,
        String nomeResponsavel,
        List<ResponsavelDto> responsaveis,
        List<ResponsavelDto> observadores,
        LocalDateTime dataInicio,
        LocalDateTime dataLimite,
        LocalDateTime dataConclusao,
        Integer criadoPorId,
        String criadoPorNome,
        List<ChecklistItemDto> subtarefas,
        List<TarefaLinkDto> links,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public record ResponsavelDto(Integer idUsuario, String nomeCompleto, String email) {}
}
