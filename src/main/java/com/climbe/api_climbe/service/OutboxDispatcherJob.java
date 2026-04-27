package com.climbe.api_climbe.service;

import com.climbe.api_climbe.config.PropriedadesNotificacao;
import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.NotificacaoOutboxRepository;
import com.climbe.api_climbe.repository.NotificacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxDispatcherJob {

    private final NotificacaoOutboxRepository outboxRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final SseService sseService;
    private final GmailApiClient gmailApiClient;
    private final EmailBodyBuilder emailBodyBuilder;
    private final PropriedadesNotificacao propriedades;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "#{${app.notificacao.dispatcher-interval-segundos:30}}000")
    @Transactional
    public void processarPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        List<NotificacaoOutbox> pendentes = outboxRepository.findPendentesParaProcessar(
            StatusEntregaNotificacao.PENDENTE, agora
        );

        if (pendentes.isEmpty()) {
            return;
        }

        int batchSize = propriedades.getBatchSize();
        List<NotificacaoOutbox> lote = pendentes.stream()
            .limit(batchSize)
            .toList();

        log.info("Processando {} entradas de outbox pendentes", lote.size());

        for (NotificacaoOutbox outbox : lote) {
            try {
                processarOutbox(outbox);
            } catch (Exception e) {
                log.error("Erro ao processar outbox {}", outbox.getIdOutbox(), e);
                handleErro(outbox, e);
            }
        }
    }

    private void processarOutbox(NotificacaoOutbox outbox) {
        CanalNotificacao canal = outbox.getCanal();
        Notificacao notificacao = outbox.getNotificacao();

        switch (canal) {
            case IN_APP -> processarInApp(outbox, notificacao);
            case SSE -> processarSse(outbox, notificacao);
            case EMAIL -> processarEmail(outbox, notificacao);
        }
    }

    private void processarInApp(NotificacaoOutbox outbox, Notificacao notificacao) {
        // IN_APP já está persistido em Notificacao, apenas marca como enviado
        outbox.setStatus(StatusEntregaNotificacao.ENVIADA);
        outbox.setUltimaTentativaEm(LocalDateTime.now());
        outbox.setAtualizadoEm(LocalDateTime.now());
        outboxRepository.save(outbox);
        log.debug("IN_APP processado para outbox {}", outbox.getIdOutbox());
    }

    private void processarSse(NotificacaoOutbox outbox, Notificacao notificacao) {
        Integer usuarioId = Integer.parseInt(outbox.getDestino());
        Map<String, Object> payload = deserializarPayload(notificacao.getPayload());
        
        sseService.enviarParaUsuario(usuarioId, "notificacao", Map.of(
            "id", notificacao.getIdNotificacao(),
            "tipo", notificacao.getTipo().name(),
            "titulo", notificacao.getTitulo(),
            "mensagem", notificacao.getMensagem(),
            "linkDestino", notificacao.getLinkDestino(),
            "payload", payload
        ));

        outbox.setStatus(StatusEntregaNotificacao.ENVIADA);
        outbox.setUltimaTentativaEm(LocalDateTime.now());
        outbox.setAtualizadoEm(LocalDateTime.now());
        outboxRepository.save(outbox);
        log.debug("SSE enviado para outbox {}", outbox.getIdOutbox());
    }

    private void processarEmail(NotificacaoOutbox outbox, Notificacao notificacao) {
        String destinatario = outbox.getDestino();
        TipoNotificacao tipo = notificacao.getTipo();
        String titulo = notificacao.getTitulo();
        String mensagem = notificacao.getMensagem();
        String linkDestino = notificacao.getLinkDestino();
        Map<String, Object> payload = deserializarPayload(notificacao.getPayload());

        String corpoHtml = emailBodyBuilder.construir(tipo, titulo, mensagem, linkDestino, payload);
        boolean enviado = gmailApiClient.enviarEmail(destinatario, tipo, titulo, corpoHtml);

        if (enviado) {
            outbox.setStatus(StatusEntregaNotificacao.ENVIADA);
            outbox.setUltimaTentativaEm(LocalDateTime.now());
            outbox.setAtualizadoEm(LocalDateTime.now());
            outboxRepository.save(outbox);
            log.debug("EMAIL enviado para outbox {}", outbox.getIdOutbox());
        } else {
            throw new RuntimeException("Falha ao enviar e-mail via Gmail API");
        }
    }

    private void handleErro(NotificacaoOutbox outbox, Exception erro) {
        int tentativas = outbox.getTentativas() + 1;
        int maxTentativas = outbox.getMaxTentativas();
        List<Duration> delays = propriedades.getDelaysRetry();

        if (tentativas >= maxTentativas) {
            outbox.setStatus(StatusEntregaNotificacao.ERRO_PERMANENTE);
            outbox.setUltimoErro(erro.getMessage());
            log.warn("Outbox {} marcado como ERRO_PERMANENTE após {} tentativas", 
                     outbox.getIdOutbox(), tentativas);
        } else {
            Duration delay = delays.get(Math.min(tentativas - 1, delays.size() - 1));
            outbox.setProximaTentativa(LocalDateTime.now().plus(delay));
            outbox.setUltimoErro(erro.getMessage());
            log.debug("Outbox {} agendado para retry em {} (tentativa {}/{})", 
                     outbox.getIdOutbox(), delay, tentativas, maxTentativas);
        }

        outbox.setTentativas(tentativas);
        outbox.setUltimaTentativaEm(LocalDateTime.now());
        outbox.setAtualizadoEm(LocalDateTime.now());
        outboxRepository.save(outbox);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializarPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payloadJson, Map.class);
        } catch (Exception e) {
            log.error("Erro ao deserializar payload", e);
            return Map.of();
        }
    }
}
