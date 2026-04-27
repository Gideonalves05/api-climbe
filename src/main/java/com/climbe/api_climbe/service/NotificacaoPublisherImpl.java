package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.PreferenciaNotificacao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.NotificacaoOutboxRepository;
import com.climbe.api_climbe.repository.NotificacaoRepository;
import com.climbe.api_climbe.repository.PreferenciaNotificacaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class NotificacaoPublisherImpl implements NotificacaoPublisher {

    private final NotificacaoRepository notificacaoRepository;
    private final NotificacaoOutboxRepository outboxRepository;
    private final PreferenciaNotificacaoRepository preferenciaRepository;
    private final SseService sseService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void publicar(
        Set<Usuario> destinatarios,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        String linkDestino,
        Map<String, Object> payload
    ) {
        String payloadJson = serializarPayload(payload);

        for (Usuario usuario : destinatarios) {
            Notificacao notificacao = criarNotificacao(usuario, tipo, titulo, mensagem, linkDestino, payloadJson);
            notificacao = notificacaoRepository.save(notificacao);

            criarOutboxParaCanais(usuario, notificacao, tipo);
        }

        log.debug("Notificação {} publicada para {} destinatários", tipo, destinatarios.size());
    }

    private Notificacao criarNotificacao(
        Usuario usuario,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        String linkDestino,
        String payloadJson
    ) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuario);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setLinkDestino(linkDestino);
        notificacao.setPayload(payloadJson);
        notificacao.setLida(false);
        notificacao.setCriadoEm(LocalDateTime.now());
        return notificacao;
    }

    private void criarOutboxParaCanais(Usuario usuario, Notificacao notificacao, TipoNotificacao tipo) {
        Integer usuarioId = usuario.getIdUsuario();
        String email = usuario.getEmail();

        // IN_APP: sempre cria (registro in-app é obrigatório)
        criarOutbox(notificacao, CanalNotificacao.IN_APP, String.valueOf(usuarioId));

        // SSE: cria se usuário tem sessão viva
        if (sseService.usuarioConectado(usuarioId)) {
            criarOutbox(notificacao, CanalNotificacao.SSE, String.valueOf(usuarioId));
        }

        // EMAIL: cria só se não houver opt-out
        if (!emailDesabilitado(usuario, tipo)) {
            criarOutbox(notificacao, CanalNotificacao.EMAIL, email);
        }
    }

    private boolean emailDesabilitado(Usuario usuario, TipoNotificacao tipo) {
        Optional<PreferenciaNotificacao> pref = preferenciaRepository
            .findByUsuarioAndTipoAndCanal(usuario, tipo, CanalNotificacao.EMAIL);
        return pref.isPresent() && !pref.get().getHabilitado();
    }

    private void criarOutbox(Notificacao notificacao, CanalNotificacao canal, String destino) {
        NotificacaoOutbox outbox = new NotificacaoOutbox();
        outbox.setNotificacao(notificacao);
        outbox.setCanal(canal);
        outbox.setDestino(destino);
        outbox.setStatus(StatusEntregaNotificacao.PENDENTE);
        outbox.setTentativas(0);
        outbox.setProximaTentativa(LocalDateTime.now());
        outbox.setCriadoEm(LocalDateTime.now());
        outbox.setAtualizadoEm(LocalDateTime.now());
        outboxRepository.save(outbox);
    }

    private String serializarPayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar payload de notificação", e);
            return null;
        }
    }
}
