package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusProposta;
import java.time.LocalDate;

public record PropostaDto(
        Integer idProposta,
        Integer idEmpresa,
        Integer idUsuarioResponsavel,
        StatusProposta status,
        String documentoProposta,
        LocalDate dataCriacao
) {
}
