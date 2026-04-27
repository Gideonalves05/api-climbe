package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "UsuarioPendente", description = "Dados de usuário pendente de aprovação")
public record UsuarioPendenteDto(
        @Schema(description = "ID do usuário", example = "1")
        Integer idUsuario,

        @Schema(description = "Nome completo", example = "João Silva")
        String nomeCompleto,

        @Schema(description = "E-mail", example = "joao.silva@climbe.com.br")
        String email,

        @Schema(description = "Cargo pretendido", example = "Analista Financeiro")
        String cargoPretendido,

        @Schema(description = "Motivo da solicitação", example = "Preciso acessar o sistema para acompanhar contratos financeiros")
        String motivo,

        @Schema(description = "Situação do usuário", example = "PENDENTE_APROVACAO")
        SituacaoUsuario situacao,

        @Schema(description = "Data/hora da solicitação", example = "2026-04-18T10:30:00")
        LocalDateTime criadoEm
) {
}
