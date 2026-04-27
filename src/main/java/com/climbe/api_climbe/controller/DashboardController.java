package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.DashboardResumoDto;
import com.climbe.api_climbe.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Resumo geral de contratos, tarefas e atividades")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obter resumo do dashboard",
            description = "Retorna estatísticas consolidadas de contratos, tarefas e atividades, filtradas por permissões do usuário.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo do dashboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResumoDto.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não autorizado")
    })
    public DashboardResumoDto obterResumo(
            @RequestParam(defaultValue = "7d") String periodo
    ) {
        return dashboardService.obterResumo(periodo);
    }
}
