package com.climbe.api_climbe.dto;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import java.time.LocalDate;

public record NotificacaoDto(
        Integer idNotificacao,
        Integer idUsuario,
        String mensagem,
        LocalDate dataEnvio,
        TipoNotificacao tipo
) {
}
