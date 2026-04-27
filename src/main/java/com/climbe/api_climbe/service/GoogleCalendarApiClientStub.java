package com.climbe.api_climbe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementação stub do GoogleCalendarApiClient.
 * Loga a ação sem criar evento real.
 * Ativa quando google.integracoes.calendar.habilitado=false (default)
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "google.integracoes.calendar", name = "habilitado", havingValue = "false", matchIfMissing = true)
public class GoogleCalendarApiClientStub implements GoogleCalendarApiClient {

    @Override
    public boolean criarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String[] participantes) {
        log.info("[CALENDAR STUB] Evento não criado (integração desativada): titulo={}, inicio={}", titulo, inicio);
        return true;
    }

    @Override
    public boolean atualizarEvento(String eventId, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim) {
        log.info("[CALENDAR STUB] Evento não atualizado (integração desativada): eventId={}, titulo={}", eventId, titulo);
        return true;
    }

    @Override
    public boolean cancelarEvento(String eventId) {
        log.info("[CALENDAR STUB] Evento não cancelado (integração desativada): eventId={}", eventId);
        return true;
    }
}
