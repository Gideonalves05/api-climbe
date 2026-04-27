package com.climbe.api_climbe.dto;

public record UsuarioResumoDto(
        Integer idUsuario,
        String nomeCompleto,
        String email,
        String cargo
) {
}
