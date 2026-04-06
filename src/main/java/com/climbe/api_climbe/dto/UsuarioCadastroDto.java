package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import java.util.Set;

public record UsuarioCadastroDto(
        String nomeCompleto,
        Integer idCargo,
        String cpf,
        String email,
        String contato,
        SituacaoUsuario situacao,
        String senha,
        Set<Integer> idsPermissoes
) {
}
