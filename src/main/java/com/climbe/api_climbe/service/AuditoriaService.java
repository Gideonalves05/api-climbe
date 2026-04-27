package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.AuditoriaEventoRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaEventoRepository auditoriaEventoRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(
            TipoEventoAuditoria tipoEvento,
            String entidade,
            Integer entidadeId,
            Map<String, Object> payload) {
        registrarEvento(tipoEvento, entidade, entidadeId, payload, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(
            TipoEventoAuditoria tipoEvento,
            String entidade,
            Integer entidadeId,
            Map<String, Object> payload,
            String correlationId) {
        try {
            Usuario ator = usuarioLogadoService.obterUsuarioLogadoOrNull();
            
            AuditoriaEvento evento = new AuditoriaEvento();
            evento.setTipoEvento(tipoEvento);
            evento.setEntidade(entidade);
            evento.setEntidadeId(entidadeId);
            evento.setAtorUsuarioId(ator != null ? ator.getIdUsuario() : null);
            evento.setAtorEmail(ator != null ? ator.getEmail() : null);
            evento.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
            evento.setCriadoEm(LocalDateTime.now());
            
            if (payload != null && !payload.isEmpty()) {
                try {
                    evento.setPayloadJson(convertPayloadToJson(payload));
                } catch (Exception e) {
                    log.warn("Erro ao converter payload para JSON no evento de auditoria: {}", e.getMessage());
                    evento.setPayloadJson("{}");
                }
            } else {
                evento.setPayloadJson("{}");
            }
            
            auditoriaEventoRepository.save(evento);
            log.info("Evento de auditoria registrado: {} - {} - {}", tipoEvento, entidade, entidadeId);
        } catch (Exception e) {
            log.error("Erro ao registrar evento de auditoria: {} - {} - {}", tipoEvento, entidade, entidadeId, e);
        }
    }

    private String convertPayloadToJson(Map<String, Object> payload) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
