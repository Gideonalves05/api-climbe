package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CriarEmpresa", description = "Payload de criação de empresa contratante")
public record CriarEmpresaDto(
        @NotBlank(message = "Nome fantasia é obrigatório")
        @Size(max = 255)
        @Schema(description = "Nome fantasia", example = "Climbe Parceira")
        String nomeFantasia,

        @Size(max = 255)
        @Schema(description = "Razão social (opcional)", example = "Climbe Parceira LTDA")
        String razaoSocial,

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "^[0-9./\\-]{14,18}$", message = "CNPJ inválido")
        @Schema(description = "CNPJ (com ou sem máscara)", example = "12.345.678/0001-90")
        String cnpj,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255)
        @Schema(description = "E-mail de contato da empresa", example = "contato@climbeparceira.com.br")
        String email,

        @Size(max = 50)
        @Schema(description = "Telefone da empresa", example = "(11) 99999-0000")
        String telefone,

        @Size(max = 255)
        @Schema(description = "Nome do representante legal", example = "João Silva")
        String representanteNome,

        @Email(message = "E-mail do representante inválido")
        @Size(max = 255)
        @Schema(description = "E-mail do representante", example = "joao@climbeparceira.com.br")
        String representanteEmail,

        @Size(max = 50)
        @Schema(description = "Telefone/contato do representante", example = "(11) 98888-7777")
        String representanteContato,

        @Size(max = 14)
        @Schema(description = "CPF do representante (opcional)", example = "123.456.789-00")
        String representanteCpf,

        @Size(max = 255)
        @Schema(description = "Logradouro", example = "Av. Paulista")
        String logradouro,
        @Size(max = 255)
        @Schema(description = "Número", example = "1000")
        String numero,
        @Size(max = 255)
        @Schema(description = "Bairro", example = "Bela Vista")
        String bairro,
        @Size(max = 255)
        @Schema(description = "Cidade", example = "São Paulo")
        String cidade,
        @Size(max = 2)
        @Schema(description = "UF", example = "SP")
        String uf,
        @Size(max = 9)
        @Schema(description = "CEP", example = "01310-100")
        String cep
) {
}
