package com.climbe.api_climbe.dto;

public record PlanilhaDto(
        Integer idPlanilha,
        Integer idContrato,
        String urlGoogleSheets,
        Boolean bloqueada,
        String permissaoVisualizacao
) {
}
