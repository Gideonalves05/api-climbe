package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PermissoesContrato", description = "Flags de permissão do usuário logado sobre um contrato")
public record PermissoesContratoDto(
        Integer idContrato,
        @Schema(description = "Pode visualizar o contrato e o Kanban")
        boolean podeVisualizar,
        @Schema(description = "Pode mover cartões, criar/editar/excluir tarefas e colunas")
        boolean podeInteragir,
        @Schema(description = "Pode adicionar/remover membros do time do contrato")
        boolean podeGerenciarTime,
        @Schema(description = "Pertence ao time ativo do contrato")
        boolean membroDoTime,
        @Schema(description = "É CEO (cargo)")
        boolean ehCeo
) {
}
