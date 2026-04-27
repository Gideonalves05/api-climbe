package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoNotificacao;

import java.util.Map;
import java.util.Set;

/**
 * Interface para publicação de eventos de notificação.
 * Implementação será fornecida na Task 4.0 (Notificações confiáveis).
 */
public interface NotificacaoPublisher {
    
    /**
     * Publica uma notificação para um conjunto de destinatários.
     * 
     * @param destinatarios Conjunto de usuários que receberão a notificação
     * @param tipo Tipo da notificação
     * @param titulo Título da notificação
     * @param mensagem Mensagem detalhada da notificação
     * @param linkDestino Link opcional para redirecionamento
     * @param payload Payload adicional com dados contextuais
     */
    void publicar(
        Set<Usuario> destinatarios,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        String linkDestino,
        Map<String, Object> payload
    );
}
