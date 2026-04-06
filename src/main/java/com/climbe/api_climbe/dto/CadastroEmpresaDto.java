package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CadastroEmpresa", description = "Dados para cadastro inicial da empresa contratante")
public record CadastroEmpresaDto(
        @NotBlank(message = "Razão social é obrigatória")
        @Schema(description = "Razão social da empresa", example = "Climbe Parceira LTDA")
        String razaoSocial,

        @NotBlank(message = "Nome fantasia é obrigatório")
        @Schema(description = "Nome fantasia", example = "Climbe Parceira")
        String nomeFantasia,

        @NotBlank(message = "CNPJ é obrigatório")
        @Schema(description = "CNPJ da empresa", example = "12.345.678/0001-90")
        String cnpj,

        @Schema(description = "Logradouro", example = "Av. Paulista")
        String logradouro,
        @Schema(description = "Número", example = "1000")
        String numero,
        @Schema(description = "Bairro", example = "Bela Vista")
        String bairro,
        @Schema(description = "Cidade", example = "São Paulo")
        String cidade,
        @Schema(description = "UF", example = "SP")
        String uf,
        @Schema(description = "CEP", example = "01310-100")
        String cep,
        @Schema(description = "Telefone", example = "(11) 99999-0000")
        String telefone,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Schema(description = "E-mail de contato da empresa", example = "contato@climbeparceira.com.br")
        String email,

        @Schema(description = "Nome do representante legal", example = "João Silva")
        String representanteNome,
        @Schema(description = "CPF do representante legal", example = "123.456.789-00")
        String representanteCpf,
        @Schema(description = "Contato do representante legal", example = "(11) 98888-7777")
        String representanteContato,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha de acesso inicial da empresa", example = "Empresa@123")
        String senha
) {
}
