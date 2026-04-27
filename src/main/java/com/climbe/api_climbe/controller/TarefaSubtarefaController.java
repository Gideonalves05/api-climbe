package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.ChecklistItemDto;
import com.climbe.api_climbe.dto.CriarChecklistItemDto;
import com.climbe.api_climbe.model.TarefaChecklistItem;
import com.climbe.api_climbe.model.TarefaContrato;
import com.climbe.api_climbe.repository.TarefaChecklistItemRepository;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import com.climbe.api_climbe.service.ContratoAutorizacaoService;
import com.climbe.api_climbe.service.TarefaContratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints para gerenciar as subtarefas (checklist) de uma tarefa do Kanban.
 * A conclusão automática da tarefa pai quando todas as subtarefas estão concluídas
 * é tratada por {@link TarefaContratoService#atualizarProgressoSubtarefas(Integer)}.
 */
@RestController
@RequestMapping("/api/contratos/{contratoId}/tarefas/{tarefaId}/subtarefas")
@RequiredArgsConstructor
@Tag(name = "Subtarefas", description = "Itens de checklist (subtarefas) de uma tarefa")
@SecurityRequirement(name = "bearerAuth")
public class TarefaSubtarefaController {

    private final TarefaChecklistItemRepository repository;
    private final TarefaContratoRepository tarefaRepository;
    private final TarefaContratoService tarefaContratoService;
    private final ContratoAutorizacaoService autorizacaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @Operation(summary = "Adicionar subtarefa")
    public ChecklistItemDto criar(@PathVariable Integer contratoId,
                                  @PathVariable Integer tarefaId,
                                  @Valid @RequestBody CriarChecklistItemDto dto) {
        autorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = obter(tarefaId, contratoId);
        int ordem = dto.ordem() != null ? dto.ordem() : repository.findByTarefa_IdTarefaOrderByOrdemAsc(tarefaId).size() + 1;
        TarefaChecklistItem item = new TarefaChecklistItem();
        item.setTarefa(tarefa);
        item.setDescricao(dto.descricao().trim());
        item.setConcluido(false);
        item.setOrdem(ordem);
        TarefaChecklistItem salvo = repository.save(item);
        return toDto(salvo);
    }

    @PatchMapping("/{itemId}")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @Operation(summary = "Atualizar subtarefa (marcar/desmarcar concluído ou editar descrição)")
    public ChecklistItemDto atualizar(@PathVariable Integer contratoId,
                                      @PathVariable Integer tarefaId,
                                      @PathVariable Integer itemId,
                                      @RequestBody AtualizarSubtarefaPayload payload) {
        autorizacaoService.exigirInteracao(contratoId);
        obter(tarefaId, contratoId);
        TarefaChecklistItem item = repository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtarefa não encontrada"));
        if (!item.getTarefa().getIdTarefa().equals(tarefaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtarefa não pertence à tarefa informada");
        }
        if (payload.descricao() != null && !payload.descricao().isBlank()) {
            item.setDescricao(payload.descricao().trim());
        }
        if (payload.concluido() != null) {
            item.setConcluido(payload.concluido());
        }
        TarefaChecklistItem salvo = repository.save(item);
        // Dispara auto-conclusão da tarefa pai se todas as subtarefas estão concluídas
        tarefaContratoService.atualizarProgressoSubtarefas(tarefaId);
        return toDto(salvo);
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover subtarefa")
    public void remover(@PathVariable Integer contratoId,
                        @PathVariable Integer tarefaId,
                        @PathVariable Integer itemId) {
        autorizacaoService.exigirInteracao(contratoId);
        obter(tarefaId, contratoId);
        TarefaChecklistItem item = repository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtarefa não encontrada"));
        if (!item.getTarefa().getIdTarefa().equals(tarefaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subtarefa não pertence à tarefa informada");
        }
        repository.delete(item);
    }

    private TarefaContrato obter(Integer tarefaId, Integer contratoId) {
        TarefaContrato tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato informado");
        }
        return tarefa;
    }

    private ChecklistItemDto toDto(TarefaChecklistItem i) {
        return new ChecklistItemDto(
                i.getIdItem(),
                i.getTarefa().getIdTarefa(),
                i.getDescricao(),
                i.getConcluido(),
                i.getOrdem(),
                i.getCriadoEm() != null ? i.getCriadoEm().toLocalDateTime() : null,
                i.getAtualizadoEm() != null ? i.getAtualizadoEm().toLocalDateTime() : null
        );
    }

    public record AtualizarSubtarefaPayload(String descricao, Boolean concluido) {}
}
