package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Implementação stub do NotificacaoPublisher.
 * A implementação real será fornecida na Task 4.0 (Notificações confiáveis).
 * Esta versão apenas loga as notificações para fins de desenvolvimento.
 */
@Service
public class NotificacaoPublisherStub implements NotificacaoPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoPublisherStub.class);

    @Override
    public void publicar(
        Set<Usuario> destinatarios,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        String linkDestino,
        Map<String, Object> payload
    ) {
        logger.info("[STUB] Notificação seria publicada:");
        logger.info("  Tipo: {}", tipo);
        logger.info("  Título: {}", titulo);
        logger.info("  Mensagem: {}", mensagem);
        logger.info("  Link: {}", linkDestino);
        logger.info("  Destinatários: {}", destinatarios.stream()
            .map(Usuario::getEmail)
            .toList());
        logger.info("  Payload: {}", payload);
        
        // TODO: Implementação real será fornecida na Task 4.0
    }
}
