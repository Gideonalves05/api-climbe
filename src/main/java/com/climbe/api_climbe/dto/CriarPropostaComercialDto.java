package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CriarPropostaComercial", description = "Dados para criação do documento de proposta comercial")
public record CriarPropostaComercialDto(
        @NotNull(message = "Empresa é obrigatória")
        @Schema(description = "ID da empresa contratante", example = "10")
        Integer idEmpresa,

        @NotBlank(message = "Documento da proposta é obrigatório")
        @Schema(description = "Conteúdo/resumo do documento de proposta comercial", example = "Proposta de prestação de serviços de valuation com escopo X, Y e Z.")
        String documentoProposta
) {
}
