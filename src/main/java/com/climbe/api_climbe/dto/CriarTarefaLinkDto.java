package com.climbe.api_climbe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarTarefaLinkDto(
        @NotBlank
        @Size(max = 500)
        String url,
        @Size(max = 160)
        String titulo
) {
}
