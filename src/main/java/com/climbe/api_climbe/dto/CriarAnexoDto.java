package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarAnexo", description = "DTO para criação de anexo em tarefa")
public record CriarAnexoDto(
    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 255, message = "Nome do arquivo deve ter no máximo 255 caracteres")
    @Schema(description = "Nome original do arquivo")
    String nomeArquivo,
    
    @Schema(description = "Tipo MIME do arquivo")
    String tipoMime,
    
    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Schema(description = "Tamanho do arquivo em bytes")
    Long tamanhoBytes,
    
    @Schema(description = "ID do arquivo no Google Drive")
    String driveFileId,
    
    @Schema(description = "URL de acesso ao arquivo (fallback)")
    String urlAcesso
) {}
