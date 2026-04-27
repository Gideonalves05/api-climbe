package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.ContratoDto;
import com.climbe.api_climbe.dto.CriarContratoDto;
import com.climbe.api_climbe.dto.PermissoesContratoDto;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.service.ContratoAutorizacaoService;
import com.climbe.api_climbe.service.ContratoService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ContratoAutorizacaoService contratoAutorizacaoService;
    private final ContratoRepository contratoRepository;

    public ContratoController(ContratoService contratoService,
                              ContratoAutorizacaoService contratoAutorizacaoService,
                              ContratoRepository contratoRepository) {
        this.contratoService = contratoService;
        this.contratoAutorizacaoService = contratoAutorizacaoService;
        this.contratoRepository = contratoRepository;
    }

    @org.springframework.web.bind.annotation.PostMapping(value = "/empresas/{idEmpresa}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTRATO_CRIAR')")
    @Operation(summary = "Criar contrato com upload de arquivo e serviço",
            security = {@SecurityRequirement(name = "bearerAuth")})
    public ContratoDto criarContratoComUpload(
            @PathVariable Integer idEmpresa,
            @RequestParam(value = "idServico", required = false) Integer idServico,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "observacoes", required = false) String observacoes,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            Authentication authentication
    ) {
        return contratoService.criarContratoComUpload(
                idEmpresa, idServico, dataInicio, dataFim, observacoes, arquivo, authentication
        );
    }

    @GetMapping("/{idContrato}/arquivo")
    @PreAuthorize("hasAuthority('CONTRATO_VER')")
    @Operation(summary = "Baixar arquivo do contrato",
            security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Integer idContrato) {
        Contrato c = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado"));
        byte[] bytes = c.getArquivoConteudo();
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato sem arquivo anexado");
        }
        String mime = c.getArquivoMime() != null ? c.getArquivoMime() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String nome = c.getArquivoNome() != null ? c.getArquivoNome() : ("contrato-" + idContrato);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header("Content-Disposition", "inline; filename=\"" + nome + "\"")
                .body(bytes);
    }

    @GetMapping("/{idContrato}/permissoes-usuario-logado")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Flags de permissão sobre o contrato",
            description = "Retorna se o usuário logado pode visualizar, interagir e gerenciar o time do contrato.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flags resolvidas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PermissoesContratoDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado")
    })
    public PermissoesContratoDto obterPermissoesDoUsuarioLogado(@PathVariable Integer idContrato) {
        return contratoAutorizacaoService.resolverFlagsUsuarioLogado(idContrato);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTRATO_CRIAR')")
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
