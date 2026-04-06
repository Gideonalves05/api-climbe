package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import java.util.Set;

public record UsuarioDto(
        Integer idUsuario,
        String nomeCompleto,
        Integer idCargo,
        String cpf,
        String email,
        String contato,
        SituacaoUsuario situacao,
        Set<Integer> idsPermissoes
) {
}
