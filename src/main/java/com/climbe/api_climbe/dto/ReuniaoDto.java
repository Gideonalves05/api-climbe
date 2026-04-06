package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusReuniao;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record ReuniaoDto(
        Integer idReuniao,
        String titulo,
        Integer idEmpresa,
        LocalDate data,
        LocalTime hora,
        Boolean presencial,
        String local,
        String pauta,
        StatusReuniao status,
        Set<Integer> idsParticipantes
) {
}
