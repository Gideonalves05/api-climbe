package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.*;
import com.climbe.api_climbe.model.TarefaContrato;
import com.climbe.api_climbe.model.TarefaDependencia;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import com.climbe.api_climbe.service.ContratoAutorizacaoService;
import com.climbe.api_climbe.service.TarefaContratoService;
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
@RequestMapping("/api/contratos/{contratoId}/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas de Contrato", description = "Endpoints para gestão de tarefas de contrato")
public class TarefaContratoController {

    private final TarefaContratoService tarefaContratoService;
    private final TarefaContratoRepository tarefaContratoRepository;
    private final ContratoAutorizacaoService contratoAutorizacaoService;

    @GetMapping
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    @Operation(summary = "Listar tarefas do contrato", description = "Retorna todas as tarefas do contrato especificado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefas listadas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public List<TarefaDto> listarTarefas(@PathVariable Integer contratoId) {
        // TODO: Implementar filtro por colunaId, responsavelId, prioridade, vencidas
        return tarefaContratoRepository.findByContrato_IdContrato(contratoId).stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TAREFA_CRIAR')")
    @Operation(summary = "Criar tarefa", description = "Cria uma nova tarefa no contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaDto criarTarefa(@PathVariable Integer contratoId, @Valid @RequestBody CriarTarefaDto dto) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato criada = tarefaContratoService.criarTarefaFromDto(contratoId, dto);
        return toDto(criada);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TAREFA_VER')")
    @Operation(summary = "Obter detalhe da tarefa", description = "Retorna os detalhes de uma tarefa específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public TarefaDto obterTarefa(@PathVariable Integer contratoId, @PathVariable Integer id) {
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        
        return toDto(tarefa);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @Operation(summary = "Atualizar tarefa", description = "Atualiza os dados de uma tarefa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public TarefaDto atualizarTarefa(@PathVariable Integer contratoId, @PathVariable Integer id, @Valid @RequestBody AtualizarTarefaDto dto) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        
        if (dto.titulo() != null) tarefa.setTitulo(dto.titulo());
        if (dto.descricao() != null) tarefa.setDescricao(dto.descricao());
        if (dto.prioridade() != null) tarefa.setPrioridade(dto.prioridade());
        if (dto.dataLimite() != null) tarefa.setDataLimite(dto.dataLimite());
        
        TarefaContrato atualizada = tarefaContratoService.atualizarTarefa(id, tarefa);
        return toDto(atualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TAREFA_EXCLUIR')")
    @Operation(summary = "Excluir tarefa", description = "Remove uma tarefa do contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
        @ApiResponse(responseCode = "400", description = "Tarefa possui dependências"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirTarefa(@PathVariable Integer contratoId, @PathVariable Integer id) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        
        tarefaContratoService.excluirTarefa(id);
    }

    @PostMapping("/{id}/mover")
    @PreAuthorize("hasAuthority('TAREFA_MOVER')")
    @Operation(summary = "Mover tarefa no Kanban", description = "Move a tarefa para a coluna destino (drag-and-drop)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa movida com sucesso"),
        @ApiResponse(responseCode = "400", description = "Coluna destino inválida ou dependências não concluídas"),
        @ApiResponse(responseCode = "403", description = "Usuário não pode interagir neste contrato"),
        @ApiResponse(responseCode = "404", description = "Tarefa ou coluna destino não encontrada")
    })
    public TarefaDto moverTarefa(@PathVariable Integer contratoId,
                                 @PathVariable Integer id,
                                 @Valid @RequestBody MoverTarefaDto dto) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        TarefaContrato movida = tarefaContratoService.moverTarefa(id, dto.colunaDestinoId());
        return toDto(movida);
    }

    @PostMapping("/{id}/dependencias")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @Operation(summary = "Adicionar dependência", description = "Adiciona uma dependência entre tarefas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dependência adicionada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou ciclo detectado"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public void adicionarDependencia(@PathVariable Integer contratoId, @PathVariable Integer id, @RequestParam Integer dependeDeId) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        
        tarefaContratoService.adicionarDependencia(id, dependeDeId);
    }

    @DeleteMapping("/{id}/dependencias/{depId}")
    @PreAuthorize("hasAuthority('TAREFA_EDITAR')")
    @Operation(summary = "Remover dependência", description = "Remove uma dependência entre tarefas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dependência removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Dependência não encontrada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerDependencia(@PathVariable Integer contratoId, @PathVariable Integer id, @PathVariable Integer depId) {
        contratoAutorizacaoService.exigirInteracao(contratoId);
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        
        if (!tarefa.getContrato().getIdContrato().equals(contratoId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarefa não pertence ao contrato especificado");
        }
        
        tarefaContratoService.removerDependencia(id, depId);
    }

    private TarefaDto toDto(TarefaContrato tarefa) {
        var principal = tarefa.getResponsavelPrincipal();
        var responsaveis = new java.util.ArrayList<TarefaDto.ResponsavelDto>();
        if (principal != null) {
            responsaveis.add(new TarefaDto.ResponsavelDto(principal.getIdUsuario(), principal.getNomeCompleto(), principal.getEmail()));
        }
        if (tarefa.getCoResponsaveis() != null) {
            tarefa.getCoResponsaveis().forEach(u -> responsaveis.add(
                    new TarefaDto.ResponsavelDto(u.getIdUsuario(), u.getNomeCompleto(), u.getEmail())
            ));
        }
        var observadores = new java.util.ArrayList<TarefaDto.ResponsavelDto>();
        if (tarefa.getObservadores() != null) {
            tarefa.getObservadores().forEach(u -> observadores.add(
                    new TarefaDto.ResponsavelDto(u.getIdUsuario(), u.getNomeCompleto(), u.getEmail())
            ));
        }
        var subtarefas = new java.util.ArrayList<ChecklistItemDto>();
        if (tarefa.getChecklistItens() != null) {
            tarefa.getChecklistItens().stream()
                    .sorted(java.util.Comparator.comparing(i -> i.getOrdem() != null ? i.getOrdem() : 0))
                    .forEach(i -> subtarefas.add(new ChecklistItemDto(
                            i.getIdItem(),
                            tarefa.getIdTarefa(),
                            i.getDescricao(),
                            i.getConcluido(),
                            i.getOrdem(),
                            i.getCriadoEm() != null ? i.getCriadoEm().toLocalDateTime() : null,
                            i.getAtualizadoEm() != null ? i.getAtualizadoEm().toLocalDateTime() : null
                    )));
        }
        var links = new java.util.ArrayList<TarefaLinkDto>();
        if (tarefa.getLinks() != null) {
            tarefa.getLinks().forEach(l -> links.add(new TarefaLinkDto(l.getIdLink(), l.getUrl(), l.getTitulo())));
        }
        return new TarefaDto(
                tarefa.getIdTarefa(),
                tarefa.getContrato().getIdContrato(),
                tarefa.getColuna().getIdColuna(),
                tarefa.getColuna().getNome(),
                tarefa.getColuna().getTipo().name(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getPrioridade(),
                principal != null ? principal.getIdUsuario() : null,
                principal != null ? principal.getNomeCompleto() : null,
                responsaveis,
                observadores,
                tarefa.getDataInicio(),
                tarefa.getDataLimite(),
                tarefa.getDataConclusao(),
                tarefa.getCriadoPor() != null ? tarefa.getCriadoPor().getIdUsuario() : null,
                tarefa.getCriadoPor() != null ? tarefa.getCriadoPor().getNomeCompleto() : null,
                subtarefas,
                links,
                tarefa.getCriadoEm() != null ? tarefa.getCriadoEm().toLocalDateTime() : null,
                tarefa.getAtualizadoEm() != null ? tarefa.getAtualizadoEm().toLocalDateTime() : null
        );
    }
}
