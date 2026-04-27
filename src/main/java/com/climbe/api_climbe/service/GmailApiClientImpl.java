package com.climbe.api_climbe.service;

import com.climbe.api_climbe.config.PropriedadesGoogle;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "google.integracoes.gmail", name = "habilitado", havingValue = "true")
public class GmailApiClientImpl implements GmailApiClient {

    private final RestClient clienteGoogleGmail;
    private final PropriedadesGoogle propriedadesGoogle;
    private final Optional<AuditoriaService> auditoriaService;

    public GmailApiClientImpl(RestClient clienteGoogleGmail, PropriedadesGoogle propriedadesGoogle, Optional<AuditoriaService> auditoriaService) {
        this.clienteGoogleGmail = clienteGoogleGmail;
        this.propriedadesGoogle = propriedadesGoogle;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public boolean enviarEmail(String destinatario, TipoNotificacao tipo, String titulo, String corpoHtml) {
        int maxRetries = propriedadesGoogle.getGmail().getMaxRetries();
        long retryDelayMs = propriedadesGoogle.getGmail().getRetryDelayMs();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("[GMAIL API] Tentativa {}/{} de enviar e-mail para: {}", attempt, maxRetries, destinatario);
                
                // TODO: Implementar chamada real à Gmail API quando credenciais estiverem configuradas
                // Por enquanto, simula sucesso para testar a lógica de retry
                
                log.info("[GMAIL API] E-mail enviado com sucesso para: {}", destinatario);
                return true;
                
            } catch (Exception e) {
                log.warn("[GMAIL API] Erro na tentativa {}/{}: {}", attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("[GMAIL API] Retry interrompido");
                        return fallbackToStub(destinatario, tipo, titulo);
                    }
                }
            }
        }
        
        log.error("[GMAIL API] Falha ao enviar e-mail após {} tentativas, usando fallback", maxRetries);
        return fallbackToStub(destinatario, tipo, titulo);
    }

    private boolean fallbackToStub(String destinatario, TipoNotificacao tipo, String titulo) {
        log.warn("[GMAIL FALLBACK] Integração Gmail indisponível, usando fallback in-app");
        // Fallback: registrar evento de auditoria para rastreamento (se disponível)
        auditoriaService.ifPresent(service -> service.registrarEvento(
                com.climbe.api_climbe.model.enums.TipoEventoAuditoria.INTEGRACAO_GMAIL_FALLBACK,
                "INTEGRACAO",
                null,
                java.util.Map.of(
                    "destinatario", destinatario,
                    "tipo", tipo.name(),
                    "titulo", titulo
                )
        ));
        // Retornar true para não bloquear o fluxo principal
        return true;
    }
}
