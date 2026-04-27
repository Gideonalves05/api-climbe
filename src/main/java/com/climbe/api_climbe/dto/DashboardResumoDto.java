package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "Resumo do dashboard com estatísticas de contratos e tarefas")
public record DashboardResumoDto(
    
    @Schema(description = "Totais de contratos por status")
    @NotNull
    ContratosTotaisDto contratosTotais,
    
    @Schema(description = "Contagem de tarefas por status")
    @NotNull
    TarefasPorStatusDto tarefasPorStatus,
    
    @Schema(description = "Número de tarefas vencendo nos próximos 7 dias")
    @NotNull
    Integer tarefasVencendoEm7Dias,
    
    @Schema(description = "Série temporal de conclusões de tarefas por dia")
    @NotNull
    List<SerieTemporalDto> conclusoesPorDia,
    
    @Schema(description = "Top 5 contratos com mais tarefas em atraso")
    @NotNull
    List<ContratoAtrasoDto> topContratosComAtraso
) {
    
    @Schema(description = "Totais de contratos por status")
    public record ContratosTotaisDto(
        @Schema(description = "Total de contratos ativos")
        Long ativos,
        @Schema(description = "Total de contratos encerrados")
        Long encerrados,
        @Schema(description = "Total de contratos suspensos")
        Long suspensos,
        @Schema(description = "Total de contratos cancelados")
        Long cancelados
    ) {}
    
    @Schema(description = "Contagem de tarefas por coluna/status")
    public record TarefasPorStatusDto(
        @Schema(description = "Tarefas em colunas INICIAL")
        Long aFazer,
        @Schema(description = "Tarefas em colunas INTERMEDIARIA")
        Long emAndamento,
        @Schema(description = "Tarefas em colunas FINAL")
        Long concluidas,
        @Schema(description = "Tarefas vencidas (data limite passada e não concluídas)")
        Long bloqueadas
    ) {}
    
    @Schema(description = "Ponto da série temporal")
    public record SerieTemporalDto(
        @Schema(description = "Data da conclusão")
        LocalDate data,
        @Schema(description = "Número de tarefas concluídas neste dia")
        Long quantidade
    ) {}
    
    @Schema(description = "Contrato com estatísticas de atraso")
    public record ContratoAtrasoDto(
        @Schema(description = "ID do contrato")
        Integer idContrato,
        @Schema(description = "Nome da empresa (proposta.empresa.nomeFantasia)")
        String nomeEmpresa,
        @Schema(description = "Número de tarefas em atraso")
        Integer tarefasEmAtraso
    ) {}
}
