package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AtualizarEmpresaDto;
import com.climbe.api_climbe.dto.ContratoResumoDto;
import com.climbe.api_climbe.dto.CriarEmpresaDto;
import com.climbe.api_climbe.dto.EmpresaDetalheDto;
import com.climbe.api_climbe.dto.EmpresaResumoDto;
import com.climbe.api_climbe.dto.PropostaResumoDto;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Listagem e detalhe de empresas contratantes")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CONTRATO_VER','EMPRESA_GERENCIAR','PROPOSTA_CRIAR','PROPOSTA_EDITAR')")
    @Operation(summary = "Listar empresas", description = "Lista paginada de empresas com busca por razão social, nome fantasia ou CNPJ")
    public Page<EmpresaResumoDto> listar(
            @RequestParam(value = "termo", required = false) String termo,
            @PageableDefault(size = 20) Pageable pageable) {
        return empresaService.listar(termo, pageable);
    }

    @GetMapping("/{idEmpresa}")
    @PreAuthorize("hasAnyAuthority('CONTRATO_VER','EMPRESA_GERENCIAR','PROPOSTA_CRIAR','PROPOSTA_EDITAR')")
    @Operation(summary = "Detalhar empresa", description = "Retorna dados cadastrais e resumo de contratos/propostas")
    public EmpresaDetalheDto detalhar(@PathVariable Integer idEmpresa) {
        return empresaService.detalhar(idEmpresa);
    }

    @GetMapping("/{idEmpresa}/contratos")
    @PreAuthorize("hasAuthority('CONTRATO_VER')")
    @Operation(summary = "Listar contratos da empresa", description = "Filtra por status opcional (VIGENTE, ENCERRADO, SUSPENSO, CANCELADO)")
    public List<ContratoResumoDto> listarContratos(
            @PathVariable Integer idEmpresa,
            @RequestParam(value = "status", required = false) StatusContrato status) {
        return empresaService.listarContratos(idEmpresa, status);
    }

    @GetMapping("/{idEmpresa}/propostas")
    @PreAuthorize("hasAnyAuthority('CONTRATO_VER','PROPOSTA_CRIAR','PROPOSTA_EDITAR')")
    @Operation(summary = "Listar propostas da empresa", description = "Retorna todas as propostas comerciais da empresa")
    public List<PropostaResumoDto> listarPropostas(@PathVariable Integer idEmpresa) {
        return empresaService.listarPropostas(idEmpresa);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EMPRESA_EDITAR')")
    @Operation(summary = "Criar empresa", description = "Cria uma nova empresa contratante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400}"))),
            @ApiResponse(responseCode = "409", description = "CNPJ ou e-mail já cadastrado",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Conflict\",\"status\":409,\"detail\":\"CNPJ já cadastrado\"}")))
    })
    public EmpresaDetalheDto criar(@Valid @RequestBody CriarEmpresaDto dto) {
        return empresaService.criar(dto);
    }

    @PutMapping("/{idEmpresa}")
    @PreAuthorize("hasAuthority('EMPRESA_EDITAR')")
    @Operation(summary = "Atualizar empresa", description = "Atualiza dados de uma empresa contratante existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "409", description = "CNPJ ou e-mail já cadastrado",
                    content = @Content(mediaType = "application/problem+json",
                            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Conflict\",\"status\":409,\"detail\":\"CNPJ já cadastrado\"}")))
    })
    public EmpresaDetalheDto atualizar(
            @PathVariable Integer idEmpresa,
            @Valid @RequestBody AtualizarEmpresaDto dto) {
        return empresaService.atualizar(idEmpresa, dto);
    }
}
