package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AdicionarMembroTimeDto;
import com.climbe.api_climbe.dto.MembroTimeDto;
import com.climbe.api_climbe.model.MembroTime;
import com.climbe.api_climbe.model.enums.PapelTime;
import com.climbe.api_climbe.service.MembroTimeService;
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
@RequestMapping("/api/contratos/{contratoId}/time")
@RequiredArgsConstructor
@Tag(name = "Time do Contrato", description = "Endpoints para gestão do time do contrato")
public class MembroTimeController {

    private final MembroTimeService membroTimeService;

    @GetMapping
    @PreAuthorize("hasAuthority('TIME_CONTRATO_VER')")
    @Operation(summary = "Listar membros do time", description = "Retorna todos os membros ativos do time do contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membros listados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public List<MembroTimeDto> listarMembros(@PathVariable Integer contratoId) {
        return membroTimeService.listarMembros(contratoId).stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TIME_CONTRATO_ADICIONAR')")
    @Operation(summary = "Adicionar membro ao time", description = "Adiciona um novo membro ao time do contrato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Membro adicionado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já é membro"),
        @ApiResponse(responseCode = "404", description = "Contrato ou usuário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public MembroTimeDto adicionarMembro(@PathVariable Integer contratoId, @Valid @RequestBody AdicionarMembroTimeDto dto) {
        MembroTime membro = membroTimeService.adicionarMembro(contratoId, dto.usuarioId(), dto.papel());
        return toDto(membro);
    }

    @GetMapping("/{membroId}")
    @PreAuthorize("hasAuthority('TIME_CONTRATO_VER')")
    @Operation(summary = "Obter membro do time", description = "Retorna os detalhes de um membro específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Membro encontrado"),
        @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public MembroTimeDto obterMembro(@PathVariable Integer contratoId, @PathVariable Integer membroId) {
        // TODO: Implementar busca por ID
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Ainda não implementado");
    }

    @DeleteMapping("/{membroId}")
    @PreAuthorize("hasAuthority('TIME_CONTRATO_REMOVER')")
    @Operation(summary = "Remover membro do time", description = "Remove um membro do time do contrato (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable Integer contratoId, @PathVariable Integer membroId) {
        membroTimeService.removerMembro(contratoId, membroId);
    }

    @PutMapping("/{membroId}/papel")
    @PreAuthorize("hasAuthority('TIME_CONTRATO_ADICIONAR')")
    @Operation(summary = "Atualizar papel do membro", description = "Atualiza o papel de um membro no time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Papel atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public MembroTimeDto atualizarPapel(@PathVariable Integer contratoId, @PathVariable Integer membroId, @RequestParam PapelTime papel) {
        MembroTime membro = membroTimeService.atualizarPapel(membroId, papel);
        return toDto(membro);
    }

    private MembroTimeDto toDto(MembroTime membro) {
        return new MembroTimeDto(
            membro.getIdMembroTime(),
            membro.getContrato().getIdContrato(),
            membro.getUsuario().getIdUsuario(),
            membro.getUsuario().getNomeCompleto(),
            membro.getUsuario().getEmail(),
            membro.getPapel(),
            membro.getDataEntrada(),
            membro.getAtivo(),
            membro.getCriadoEm().toLocalDateTime(),
            membro.getAtualizadoEm() != null ? membro.getAtualizadoEm().toLocalDateTime() : null
        );
    }
}
