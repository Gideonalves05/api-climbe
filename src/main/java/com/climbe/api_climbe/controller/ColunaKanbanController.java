package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.*;
import com.climbe.api_climbe.model.ColunaKanban;
import com.climbe.api_climbe.repository.ColunaKanbanRepository;
import com.climbe.api_climbe.service.ColunaKanbanService;
import com.climbe.api_climbe.service.ContratoAutorizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contratos/{contratoId}/colunas")
@RequiredArgsConstructor
@Tag(name = "Colunas Kanban", description = "Endpoints para gestão de colunas do Kanban")
public class ColunaKanbanController {

    private final ColunaKanbanService colunaKanbanService;
    private final ColunaKanbanRepository colunaKanbanRepository;
    private final ContratoAutorizacaoService contratoAutorizacaoService;

    @GetMapping
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    @Operation(summary = "Listar colunas do contrato", description = "Retorna todas as colunas do Kanban do contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colunas listadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public List<ColunaDto> listarColunas(@PathVariable Integer contratoId) {
        return colunaKanbanRepository.findByContrato_IdContratoOrderByOrdemAsc(contratoId).stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('KANBAN_GERENCIAR_COLUNAS')")
    @Operation(summary = "Criar coluna", description = "Cria uma nova coluna no Kanban do contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Coluna criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ColunaDto criarColuna(@PathVariable Integer contratoId, @Valid @RequestBody CriarColunaDto dto) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        ColunaKanban coluna = new ColunaKanban();
        coluna.setNome(dto.nome());
        coluna.setOrdem(dto.ordem());
        coluna.setTipo(dto.tipo());
        coluna.setCor(dto.cor());
        
        ColunaKanban criada = colunaKanbanService.criarColuna(contratoId, coluna);
        return toDto(criada);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    @Operation(summary = "Obter coluna", description = "Retorna os detalhes de uma coluna específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coluna encontrada"),
        @ApiResponse(responseCode = "404", description = "Coluna não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ColunaDto obterColuna(@PathVariable Integer contratoId, @PathVariable Integer id) {
        ColunaKanban coluna = colunaKanbanRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Coluna não encontrada"));
        
        if (!coluna.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Coluna não pertence ao contrato especificado");
        }
        
        return toDto(coluna);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('KANBAN_GERENCIAR_COLUNAS')")
    @Operation(summary = "Atualizar coluna", description = "Atualiza os dados de uma coluna")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coluna atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Coluna não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ColunaDto atualizarColuna(@PathVariable Integer contratoId, @PathVariable Integer id, @Valid @RequestBody AtualizarColunaDto dto) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        ColunaKanban coluna = colunaKanbanRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Coluna não encontrada"));
        
        if (!coluna.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Coluna não pertence ao contrato especificado");
        }
        
        if (dto.nome() != null) coluna.setNome(dto.nome());
        if (dto.ordem() != null) coluna.setOrdem(dto.ordem());
        if (dto.tipo() != null) coluna.setTipo(dto.tipo());
        if (dto.cor() != null) coluna.setCor(dto.cor());
        
        ColunaKanban atualizada = colunaKanbanService.atualizarColuna(id, coluna);
        return toDto(atualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('KANBAN_GERENCIAR_COLUNAS')")
    @Operation(summary = "Excluir coluna", description = "Remove uma coluna do Kanban")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Coluna excluída com sucesso"),
        @ApiResponse(responseCode = "400", description = "Coluna possui tarefas vinculadas"),
        @ApiResponse(responseCode = "404", description = "Coluna não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirColuna(@PathVariable Integer contratoId, @PathVariable Integer id) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        ColunaKanban coluna = colunaKanbanRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Coluna não encontrada"));
        
        if (!coluna.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Coluna não pertence ao contrato especificado");
        }
        
        colunaKanbanService.excluirColuna(id);
    }

    @PutMapping("/reordenar")
    @PreAuthorize("hasAuthority('KANBAN_GERENCIAR_COLUNAS')")
    @Operation(summary = "Reordenar colunas", description = "Atualiza a ordem das colunas em lote")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colunas reordenadas com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public void reordenarColunas(@PathVariable Integer contratoId, @Valid @RequestBody List<AtualizarColunaDto> colunas) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        List<ColunaKanban> colunasKanban = colunas.stream().map(dto -> {
            ColunaKanban coluna = new ColunaKanban();
            coluna.setIdColuna(null); // Será preenchido pelo repository
            // TODO: Precisamos passar os IDs das colunas para reordenar
            return coluna;
        }).toList();
        
        colunaKanbanService.reordenarColunas(contratoId, colunasKanban);
    }

    @GetMapping("/kanban")
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    @Operation(summary = "Obter board Kanban montado", description = "Retorna o board Kanban com colunas e tarefas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Board Kanban retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public KanbanBoardDto obterKanbanBoard(@PathVariable Integer contratoId) {
        List<ColunaKanban> colunas = colunaKanbanRepository.findByContrato_IdContratoOrderByOrdemAsc(contratoId);
        
        List<KanbanBoardDto.ColunaComTarefasDto> colunasComTarefas = colunas.stream()
            .map(coluna -> new KanbanBoardDto.ColunaComTarefasDto(
                toDto(coluna),
                List.of() // TODO: Carregar tarefas por coluna
            ))
            .toList();
        
        return new KanbanBoardDto(colunasComTarefas);
    }

    private ColunaDto toDto(ColunaKanban coluna) {
        return new ColunaDto(
            coluna.getIdColuna(),
            coluna.getContrato().getIdContrato(),
            coluna.getNome(),
            coluna.getOrdem(),
            coluna.getTipo(),
            coluna.getCor(),
            coluna.getCriadoEm().toLocalDateTime(),
            coluna.getAtualizadoEm() != null ? coluna.getAtualizadoEm().toLocalDateTime() : null
        );
    }
}
