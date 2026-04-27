package com.climbe.api_climbe.service;

import java.time.LocalDateTime;

/**
 * Cliente para integração com Google Calendar API.
 * Integração desativada por padrão (propriedades GOOGLE_CALENDAR_HABILITADO=false).
 */
public interface GoogleCalendarApiClient {
    
    /**
     * Cria um evento no Google Calendar.
     * 
     * @param titulo Título do evento
     * @param descricao Descrição do evento
     * @param inicio Data/hora de início
     * @param fim Data/hora de fim
     * @param participantes Lista de e-mails dos participantes
     * @return true se criado com sucesso, false caso contrário
     */
    boolean criarEvento(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String[] participantes);
    
    /**
     * Atualiza um evento existente no Google Calendar.
     * 
     * @param eventId ID do evento no Google Calendar
     * @param titulo Novo título
     * @param descricao Nova descrição
     * @param inicio Nova data/hora de início
     * @param fim Nova data/hora de fim
     * @return true se atualizado com sucesso, false caso contrário
     */
    boolean atualizarEvento(String eventId, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Cancela um evento no Google Calendar.
     * 
     * @param eventId ID do evento no Google Calendar
     * @return true se cancelado com sucesso, false caso contrário
     */
    boolean cancelarEvento(String eventId);
}
