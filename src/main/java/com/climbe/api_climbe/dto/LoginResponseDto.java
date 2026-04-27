package com.climbe.api_climbe.dto;

public record LoginResponseDto(
    String token,
    String tokenType,
    Long expiresIn,
    String tipoConta,
    String idUsuario,
    String nome,
    String cargo,
    String email
) {}
