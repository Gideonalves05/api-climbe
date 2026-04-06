package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.CriarPropostaComercialDto;
import com.climbe.api_climbe.dto.DecisaoPropostaDto;
import com.climbe.api_climbe.dto.PropostaDto;
import com.climbe.api_climbe.service.PropostaComercialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/propostas")
@Tag(name = "Propostas Comerciais", description = "Fluxo da proposta comercial após reunião com contratante")
public class PropostaComercialController {

    private final PropostaComercialService propostaComercialService;

    public PropostaComercialController(PropostaComercialService propostaComercialService) {
        this.propostaComercialService = propostaComercialService;
    }

    @PostMapping("/comercial")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar proposta comercial",
            description = "Cria o documento de proposta comercial para a empresa cadastrada.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropostaDto.class))),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido somente para funcionário Climbe")
    })
    public PropostaDto criarProposta(
            @Valid @RequestBody CriarPropostaComercialDto dto,
            Authentication authentication
    ) {
        return propostaComercialService.criarProposta(dto, authentication);
    }

    @PatchMapping("/{idProposta}/decisao")
    @Operation(
            summary = "Aprovar ou recusar proposta",
            description = "Registra a decisão da proposta. Quando aprovada, deve informar o funcionário responsável por criar o contrato.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decisão registrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PropostaDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na decisão",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Ao aprovar a proposta, informe o funcionário responsável pela criação do contrato\"}"))),
            @ApiResponse(responseCode = "404", description = "Proposta ou funcionário não encontrado")
    })
    public PropostaDto decidirProposta(
            @PathVariable Integer idProposta,
            @Valid @RequestBody DecisaoPropostaDto dto,
            Authentication authentication
    ) {
        return propostaComercialService.decidirProposta(idProposta, dto, authentication);
    }
}
