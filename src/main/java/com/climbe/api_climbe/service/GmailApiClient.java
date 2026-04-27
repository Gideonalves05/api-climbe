package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import java.util.Map;

/**
 * Cliente para envio de e-mails via Gmail API.
 * Integração real desativada por enquanto (propriedades GOOGLE_GMAIL_HABILITADO=false).
 * Esta implementação é um stub que loga a ação sem enviar e-mail real.
 */
public interface GmailApiClient {
    
    /**
     * Envia um e-mail via Gmail API.
     * 
     * @param destinatario Endereço de e-mail do destinatário
     * @param tipo Tipo de notificação
     * @param titulo Título/assunto do e-mail
     * @param corpoHtml Corpo do e-mail em HTML
     * @return true se enviado com sucesso, false caso contrário
     */
    boolean enviarEmail(String destinatario, TipoNotificacao tipo, String titulo, String corpoHtml);
}
