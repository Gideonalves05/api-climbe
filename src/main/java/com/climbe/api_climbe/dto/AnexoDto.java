package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Anexo", description = "DTO de anexo de tarefa")
public record AnexoDto(
    @Schema(description = "ID do anexo")
    Integer idAnexo,
    
    @Schema(description = "ID da tarefa")
    Integer tarefaId,
    
    @Schema(description = "Nome do arquivo")
    String nomeArquivo,
    
    @Schema(description = "Tipo MIME do arquivo")
    String tipoMime,
    
    @Schema(description = "Tamanho do arquivo em bytes")
    Long tamanhoBytes,
    
    @Schema(description = "ID do arquivo no Google Drive")
    String driveFileId,
    
    @Schema(description = "URL de acesso ao arquivo")
    String urlAcesso,
    
    @Schema(description = "ID do usuário que fez o upload")
    Integer uploadPorId,
    
    @Schema(description = "Nome do usuário que fez o upload")
    String uploadPorNome,
    
    @Schema(description = "Data de upload")
    java.time.LocalDateTime uploadEm
) {}
