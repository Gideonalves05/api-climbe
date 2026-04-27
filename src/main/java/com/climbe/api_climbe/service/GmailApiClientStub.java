package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementação stub do GmailApiClient.
 * Loga a ação sem enviar e-mail real.
 * Ativa quando google.integracoes.gmail.habilitado=false (default)
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "google.integracoes.gmail", name = "habilitado", havingValue = "false", matchIfMissing = true)
public class GmailApiClientStub implements GmailApiClient {

    @Override
    public boolean enviarEmail(String destinatario, TipoNotificacao tipo, String titulo, String corpoHtml) {
        log.info("[GMAIL STUB] E-mail não enviado (integração desativada): destinatario={}, tipo={}, titulo={}", 
                 destinatario, tipo, titulo);
        // Stub sempre retorna sucesso para não bloquear o fluxo
        return true;
    }
}
