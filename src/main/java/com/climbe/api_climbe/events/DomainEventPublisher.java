package com.climbe.api_climbe.events;

/**
 * Interface para publicação de eventos de domínio.
 * Esta interface prepara o sistema para a implementação de notificações (Task 4.0).
 * 
 * Eventos de domínio que serão publicados:
 * - Tarefa criada
 * - Tarefa movida para nova coluna (incluindo conclusão)
 * - Novo comentário
 * - Novo anexo
 * - Dependência adicionada/removida
 * - Prazo se aproximando (72h, 24h, ao vencer)
 * 
 * A implementação concreta será criada na Task 4.0 com Outbox Pattern.
 */
public interface DomainEventPublisher {
    
    /**
     * Publica um evento de domínio de forma assíncrona.
     * 
     * @param evento O evento a ser publicado
     */
    void publish(DomainEvent evento);
    
    /**
     * Interface base para todos os eventos de domínio.
     */
    interface DomainEvent {
        String getEventType();
        java.time.LocalDateTime getOccurredAt();
        Object getPayload();
    }
}
