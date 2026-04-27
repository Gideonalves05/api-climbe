package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusContrato;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "ContratoResumo", description = "Resumo de contrato para listagens por empresa")
public record ContratoResumoDto(
        Integer idContrato,
        Integer idProposta,
        Integer idEmpresa,
        String nomeEmpresa,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusContrato status,
        Integer idResponsavel,
        String nomeResponsavel,
        Integer idServico,
        String nomeServico,
        boolean possuiArquivo,
        String arquivoNome
) {
}
