package com.climbe.api_climbe.dto;

import java.time.LocalDate;

public record RelatorioDto(
        Integer idRelatorio,
        Integer idContrato,
        String urlPdf,
        LocalDate dataEnvio
) {
}
