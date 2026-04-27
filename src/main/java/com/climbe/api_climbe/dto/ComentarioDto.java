package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Comentario", description = "DTO de comentário de tarefa")
public record ComentarioDto(
    @Schema(description = "ID do comentário")
    Integer idComentario,
    
    @Schema(description = "ID da tarefa")
    Integer tarefaId,
    
    @Schema(description = "ID do autor")
    Integer autorId,
    
    @Schema(description = "Nome do autor")
    String autorNome,
    
    @Schema(description = "Email do autor")
    String autorEmail,
    
    @Schema(description = "Texto do comentário")
    String texto,
    
    @Schema(description = "Data de criação")
    java.time.LocalDateTime criadoEm,
    
    @Schema(description = "Data de edição")
    java.time.LocalDateTime editadoEm
) {}
