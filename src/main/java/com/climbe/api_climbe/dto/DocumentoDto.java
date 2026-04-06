package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.StatusValidacaoDocumento;
import com.climbe.api_climbe.model.enums.TipoDocumento;

public record DocumentoDto(
        Integer idDocumento,
        Integer idEmpresa,
        TipoDocumento tipoDocumento,
        String url,
        StatusValidacaoDocumento validado
) {
}
