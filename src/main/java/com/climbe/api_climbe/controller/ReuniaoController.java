package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AgendarReuniaoContratanteDto;
import com.climbe.api_climbe.dto.ReuniaoDto;
import com.climbe.api_climbe.service.ReuniaoService;
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
@RequestMapping("/api/reunioes")
@Tag(name = "Reuniões", description = "Endpoints de reuniões com empresas contratantes")
public class ReuniaoController {

    private final ReuniaoService reuniaoService;

    public ReuniaoController(ReuniaoService reuniaoService) {
        this.reuniaoService = reuniaoService;
    }

    @PostMapping("/contratante")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Agendar reunião com contratante",
            description = "Após o cadastro da empresa, um funcionário da Climbe agenda a reunião de atendimento inicial.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reunião agendada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReuniaoDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Token não pertence a funcionário da Climbe",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Somente funcionário da Climbe pode agendar reunião com contratante\"}"))),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Empresa não encontrada\"}")))
    })
    public ReuniaoDto agendarReuniaoContratante(
            @Valid @RequestBody AgendarReuniaoContratanteDto dto,
            Authentication authentication
    ) {
        return reuniaoService.agendarComContratante(dto, authentication);
    }
}
