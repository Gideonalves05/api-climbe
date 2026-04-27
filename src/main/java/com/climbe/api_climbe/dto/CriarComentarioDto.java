package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarComentario", description = "DTO para criação de comentário em tarefa")
public record CriarComentarioDto(
    @NotBlank(message = "Texto do comentário é obrigatório")
    @Size(max = 5000, message = "Texto deve ter no máximo 5000 caracteres")
    @Schema(description = "Texto do comentário", example = "Preciso de mais informações sobre este documento")
    String texto
) {}
