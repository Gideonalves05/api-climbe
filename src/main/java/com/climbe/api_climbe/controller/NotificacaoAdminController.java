package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.NotificacaoOutboxDto;
import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import com.climbe.api_climbe.repository.NotificacaoOutboxRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Admin - Notificações", description = "Endpoints administrativos para gestão de outbox de notificações")
@RestController
@RequestMapping("/api/admin/notificacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoAdminController {

    private final NotificacaoOutboxRepository outboxRepository;

    @GetMapping("/outbox")
    @Operation(summary = "Listar outbox de notificações", description = "Retorna entradas de outbox com filtros opcionais")
    public List<NotificacaoOutboxDto> listarOutbox(
        @RequestParam(required = false) StatusEntregaNotificacao status
    ) {
        List<NotificacaoOutbox> outboxEntries;
        
        if (status != null) {
            outboxEntries = outboxRepository.findByStatusAndProximaTentativaLessThanEqualOrderByProximaTentativaAsc(
                status, LocalDateTime.now()
            );
        } else {
            outboxEntries = outboxRepository.findAll();
        }

        return outboxEntries.stream()
            .map(this::paraDto)
            .collect(Collectors.toList());
    }

    @PostMapping("/outbox/{id}/retry")
    @Operation(summary = "Reprocessar entrada de outbox", description = "Coloca uma entrada com erro permanente de volta para processamento")
    public void retryOutbox(@PathVariable Long id) {
        NotificacaoOutbox outbox = outboxRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada de outbox não encontrada"));

        if (outbox.getStatus() != StatusEntregaNotificacao.ERRO_PERMANENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Apenas entradas com ERRO_PERMANENTE podem ser reprocessadas");
        }

        outbox.setStatus(StatusEntregaNotificacao.PENDENTE);
        outbox.setTentativas(0);
        outbox.setProximaTentativa(LocalDateTime.now());
        outbox.setUltimoErro(null);
        outbox.setAtualizadoEm(LocalDateTime.now());
        
        outboxRepository.save(outbox);
    }

    private NotificacaoOutboxDto paraDto(NotificacaoOutbox o) {
        return new NotificacaoOutboxDto(
            o.getIdOutbox(),
            o.getNotificacao().getIdNotificacao(),
            o.getCanal(),
            o.getDestino(),
            o.getStatus(),
            o.getTentativas(),
            o.getMaxTentativas(),
            o.getProximaTentativa(),
            o.getUltimaTentativaEm(),
            o.getUltimoErro(),
            o.getCriadoEm(),
            o.getAtualizadoEm()
        );
    }
}
