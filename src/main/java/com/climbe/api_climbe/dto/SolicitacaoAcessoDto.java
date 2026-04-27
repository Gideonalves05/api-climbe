package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SolicitacaoAcesso", description = "Dados para solicitação de acesso ao sistema")
public record SolicitacaoAcessoDto(
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(max = 255, message = "Nome completo deve ter no máximo 255 caracteres")
        @Schema(description = "Nome completo do solicitante", example = "João Silva")
        String nomeCompleto,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Schema(description = "E-mail corporativo do solicitante", example = "joao.silva@climbe.com.br")
        String email,

        @NotBlank(message = "Cargo/função pretendido é obrigatório")
        @Size(max = 255, message = "Cargo/função pretendido deve ter no máximo 255 caracteres")
        @Schema(description = "Cargo ou função pretendida pelo solicitante", example = "Analista Financeiro")
        String cargoPretendido,

        @NotBlank(message = "Motivo da solicitação é obrigatório")
        @Size(max = 1000, message = "Motivo da solicitação deve ter no máximo 1000 caracteres")
        @Schema(description = "Motivo pelo qual o solicitante deseja acesso ao sistema", example = "Preciso acessar o sistema para acompanhar contratos financeiros da área BPO")
        String motivo
) {
}
