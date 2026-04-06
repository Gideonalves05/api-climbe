package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Schema(name = "AgendarReuniaoContratante", description = "Dados para agendamento da reunião entre Climbe e empresa contratante")
public record AgendarReuniaoContratanteDto(
        @NotNull(message = "Empresa é obrigatória")
        @Schema(description = "ID da empresa cadastrada", example = "10")
        Integer idEmpresa,

        @NotBlank(message = "Título é obrigatório")
        @Schema(description = "Título da reunião", example = "Reunião inicial de levantamento")
        String titulo,

        @NotNull(message = "Data é obrigatória")
        @FutureOrPresent(message = "Data deve ser hoje ou futura")
        @Schema(description = "Data da reunião", example = "2026-03-24")
        LocalDate data,

        @NotNull(message = "Hora é obrigatória")
        @Schema(description = "Hora da reunião", example = "14:30:00")
        LocalTime hora,

        @NotNull(message = "Informe se é presencial")
        @Schema(description = "Se a reunião será presencial", example = "true")
        Boolean presencial,

        @Schema(description = "Local da reunião presencial ou link da chamada", example = "Sala 02 - Sede Climbe")
        String local,

        @Schema(description = "Pauta da reunião", example = "Entendimento de necessidades e próximos passos")
        String pauta,

        @Schema(description = "IDs de funcionários adicionais da Climbe para participar")
        Set<Integer> idsParticipantes
) {
}
