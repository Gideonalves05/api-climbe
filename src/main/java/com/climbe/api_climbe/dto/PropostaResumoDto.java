package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusProposta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "PropostaResumo", description = "Resumo de proposta comercial para listagem")
public record PropostaResumoDto(
        Integer idProposta,
        Integer idEmpresa,
        String nomeEmpresa,
        Integer idResponsavel,
        String nomeResponsavel,
        StatusProposta status,
        LocalDate dataCriacao,
        boolean possuiDocumento,
        Integer idServico,
        String nomeServico,
        BigDecimal valor,
        LocalDate dataValidade,
        boolean possuiArquivo
) {
}
