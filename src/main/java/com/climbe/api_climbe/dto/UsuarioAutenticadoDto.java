package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "UsuarioAutenticado", description = "Dados do usuário autenticado, incluindo cargo e permissões efetivas.")
public record UsuarioAutenticadoDto(
        @Schema(description = "Identificador do usuário", example = "1")
        Integer id,

        @Schema(description = "E-mail do usuário", example = "ana.souza@climbe.com.br")
        String email,

        @Schema(description = "Nome completo", example = "Ana Souza")
        String nomeCompleto,

        @Schema(description = "Cargo vinculado ao usuário")
        CargoResumoDto cargo,

        @Schema(description = "Códigos das permissões efetivas do usuário")
        List<String> permissoes
) {
    @Schema(name = "CargoResumo", description = "Resumo do cargo do usuário")
    public record CargoResumoDto(
            @Schema(description = "Identificador do cargo", example = "1")
            Integer id,

            @Schema(description = "Nome do cargo", example = "ANALISTA")
            String nomeCargo
    ) {
    }
}
