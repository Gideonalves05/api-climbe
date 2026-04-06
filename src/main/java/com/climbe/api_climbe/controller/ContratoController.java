package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.ContratoDto;
import com.climbe.api_climbe.dto.CriarContratoDto;
import com.climbe.api_climbe.service.ContratoService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos")
@Tag(name = "Contratos", description = "Criação de contrato após proposta aprovada")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar contrato",
            description = "Cria contrato a partir de proposta aprovada, somente pelo funcionário selecionado na aprovação.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contrato criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContratoDto.class))),
            @ApiResponse(responseCode = "400", description = "Proposta não aprovada"),
            @ApiResponse(responseCode = "403", description = "Funcionário não autorizado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Apenas o funcionário selecionado na aprovação da proposta pode criar o contrato\"}"))),
            @ApiResponse(responseCode = "409", description = "Contrato já existente para a proposta")
    })
    public ContratoDto criarContrato(
            @Valid @RequestBody CriarContratoDto dto,
            Authentication authentication
    ) {
        return contratoService.criarContrato(dto, authentication);
    }
}
