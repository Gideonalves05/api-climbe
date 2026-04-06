package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusContrato;
import java.time.LocalDate;

public record ContratoDto(
        Integer idContrato,
        Integer idProposta,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusContrato status
) {
}
