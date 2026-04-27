package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmpresaDetalhe", description = "Detalhe completo da empresa com contagens")
public record EmpresaDetalheDto(
        Integer idEmpresa,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String uf,
        String cep,
        String telefone,
        String email,
        String representanteNome,
        String representanteCpf,
        String representanteContato,
        String representanteEmail,
        long totalContratosVigentes,
        long totalContratosEncerrados,
        long totalPropostas
) {
}
