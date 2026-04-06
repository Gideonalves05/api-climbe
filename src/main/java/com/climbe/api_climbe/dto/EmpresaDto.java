package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(name = "Empresa", description = "Representação da empresa cadastrada")
public record EmpresaDto(
        @Schema(description = "ID da empresa", example = "10")
        Integer idEmpresa,
        @Schema(description = "Razão social", example = "Climbe Parceira LTDA")
        String razaoSocial,
        @Schema(description = "Nome fantasia", example = "Climbe Parceira")
        String nomeFantasia,
        @Schema(description = "CNPJ normalizado", example = "12345678000190")
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
        @Schema(description = "E-mail de contato", example = "contato@climbeparceira.com.br")
        String email,
        @Schema(description = "Representante legal", example = "João Silva")
        String representanteNome,
        @Schema(description = "CPF do representante", example = "123.456.789-00")
        String representanteCpf,
        @Schema(description = "Contato do representante", example = "(11) 98888-7777")
        String representanteContato,
        @Schema(description = "IDs dos serviços associados")
        Set<Integer> idsServicos
) {
}
