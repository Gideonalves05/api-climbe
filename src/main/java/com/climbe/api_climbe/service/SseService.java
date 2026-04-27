package com.climbe.api_climbe.service;

import com.climbe.api_climbe.config.PropriedadesNotificacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final PropriedadesNotificacao propriedades;
    private final ObjectMapper objectMapper;

    private final Map<Integer, CopyOnWriteArrayList<SseEmitter>> emittersPorUsuario = new ConcurrentHashMap<>();

    /**
     * Cria um novo emitter SSE para o usuário.
     */
    public SseEmitter criarEmitter(Integer usuarioId) {
        Duration timeout = propriedades.getSseTimeout();
        SseEmitter emitter = new SseEmitter(timeout.toMillis());

        emittersPorUsuario
            .computeIfAbsent(usuarioId, k -> new CopyOnWriteArrayList<>())
            .add(emitter);

        emitter.onCompletion(() -> removerEmitter(usuarioId, emitter));
        emitter.onTimeout(() -> removerEmitter(usuarioId, emitter));
        emitter.onError((ex) -> {
            log.warn("Erro no emitter SSE do usuário {}", usuarioId, ex);
            removerEmitter(usuarioId, emitter);
        });

        log.debug("Emitter SSE criado para usuário {}. Total de emitters ativos: {}", 
                 usuarioId, emittersPorUsuario.getOrDefault(usuarioId, new CopyOnWriteArrayList<>()).size());

        return emitter;
    }

    /**
     * Envia um evento para todos os emitters de um usuário.
     */
    public void enviarParaUsuario(Integer usuarioId, String nomeEvento, Object dados) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersPorUsuario.get(usuarioId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("Nenhum emitter SSE ativo para usuário {}", usuarioId);
            return;
        }

        try {
            String dadosJson = objectMapper.writeValueAsString(dados);
            emitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name(nomeEvento)
                        .data(dadosJson));
                    return false;
                } catch (IOException e) {
                    log.warn("Falha ao enviar evento SSE para usuário {}", usuarioId, e);
                    return true;
                }
            });
            log.debug("Evento {} enviado para usuário {} ({} emitters ativos)", 
                     nomeEvento, usuarioId, emitters.size());
        } catch (Exception e) {
            log.error("Erro ao serializar dados para SSE do usuário {}", usuarioId, e);
        }
    }

    /**
     * Verifica se o usuário tem pelo menos um emitter ativo.
     */
    public boolean usuarioConectado(Integer usuarioId) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersPorUsuario.get(usuarioId);
        return emitters != null && !emitters.isEmpty();
    }

    /**
     * Remove um emitter da lista do usuário.
     */
    private void removerEmitter(Integer usuarioId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersPorUsuario.get(usuarioId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersPorUsuario.remove(usuarioId);
            }
        }
        log.debug("Emitter SSE removido para usuário {}. Total de emitters ativos: {}", 
                 usuarioId, emittersPorUsuario.getOrDefault(usuarioId, new CopyOnWriteArrayList<>()).size());
    }

    /**
     * Heartbeat para manter conexões vivas através de proxies.
     * Envia um comment a cada intervalo configurado.
     */
    @Scheduled(fixedRateString = "${app.notificacao.sse-heartbeat-interval:25s}")
    public void heartbeat() {
        emittersPorUsuario.forEach((usuarioId, emitters) -> {
            emitters.removeIf(emitter -> {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                    return false;
                } catch (IOException e) {
                    log.debug("Emitter removido durante heartbeat para usuário {}", usuarioId);
                    return true;
                }
            });
        });
    }

    /**
     * Retorna o número total de conexões SSE ativas.
     */
    public int getTotalConexoes() {
        return emittersPorUsuario.values().stream()
            .mapToInt(java.util.List::size)
            .sum();
    }
}
