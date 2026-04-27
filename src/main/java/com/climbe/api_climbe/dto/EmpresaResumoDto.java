package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmpresaResumo", description = "Resumo da empresa para listagens")
public record EmpresaResumoDto(
        Integer idEmpresa,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String cidade,
        String uf,
        String email,
        long totalContratos,
        long contratosVigentes
) {
}
