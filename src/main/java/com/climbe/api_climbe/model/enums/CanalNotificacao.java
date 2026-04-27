package com.climbe.api_climbe.model.enums;

/**
 * Canais de entrega de notificação.
 * IN_APP = persistido em Notificacao e lido via REST.
 * SSE = push instantâneo para sessão ativa do usuário (tem TTL — não persiste fora do outbox).
 * EMAIL = envio assíncrono via Gmail API.
 */
public enum CanalNotificacao {
    IN_APP,
    EMAIL,
    SSE
}
