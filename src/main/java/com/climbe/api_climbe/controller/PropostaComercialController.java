package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.CriarPropostaComercialDto;
import com.climbe.api_climbe.dto.DecisaoPropostaDto;
import com.climbe.api_climbe.dto.PropostaDto;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.service.PropostaComercialService;
import com.climbe.api_climbe.service.PropostaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final PropostaService propostaService;
    private final PropostaRepository propostaRepository;

    public PropostaComercialController(PropostaComercialService propostaComercialService,
                                       PropostaService propostaService,
                                       PropostaRepository propostaRepository) {
        this.propostaComercialService = propostaComercialService;
        this.propostaService = propostaService;
        this.propostaRepository = propostaRepository;
    }

    @GetMapping("/{idProposta}/documento")
    @PreAuthorize("hasAnyAuthority('CONTRATO_VER','PROPOSTA_CRIAR','PROPOSTA_EDITAR')")
    @Operation(
            summary = "Obter documento da proposta",
            description = "Retorna o documento da proposta como PDF binário (novo) ou texto legado (retrocompatível).",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento obtido",
                    content = {@Content(mediaType = "application/pdf"), @Content(mediaType = "text/plain")}),
            @ApiResponse(responseCode = "404", description = "Proposta ou documento não encontrado")
    })
    public ResponseEntity<byte[]> obterDocumento(@PathVariable Integer idProposta) {
        PropostaService.DocumentoResultado resultado = propostaService.obterDocumento(idProposta);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(resultado.contentType()));
        headers.setContentDispositionFormData("inline", resultado.nomeArquivo());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resultado.conteudo());
    }

    @GetMapping("/{idProposta}/arquivo")
    @PreAuthorize("hasAnyAuthority('CONTRATO_VER','PROPOSTA_CRIAR','PROPOSTA_EDITAR')")
    @Operation(summary = "Baixar arquivo binário da proposta",
            security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Integer idProposta) {
        Proposta proposta = propostaRepository.findById(idProposta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta não encontrada"));
        byte[] bytes = proposta.getArquivoConteudo();
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposta sem arquivo anexado");
        }
        String mime = proposta.getArquivoMime() != null ? proposta.getArquivoMime() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String nome = proposta.getArquivoNome() != null ? proposta.getArquivoNome() : ("proposta-" + idProposta);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header("Content-Disposition", "inline; filename=\"" + nome + "\"")
                .body(bytes);
    }

    @PostMapping(value = "/empresas/{idEmpresa}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PROPOSTA_CRIAR')")
    @Operation(summary = "Criar proposta com upload de arquivo PDF e serviço",
            description = "Cria proposta com upload de PDF (max 10MB). O serviço é obrigatório.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou arquivo não é PDF/ultrapassa 10MB"),
            @ApiResponse(responseCode = "404", description = "Empresa ou serviço não encontrado")
    })
    public PropostaDto criarPropostaCompleta(
            @PathVariable Integer idEmpresa,
            @RequestParam(value = "servicoId") Integer servicoId,
            @RequestParam(value = "valor", required = false) BigDecimal valor,
            @RequestParam(value = "observacoes", required = false) String observacoes,
            @RequestParam(value = "dataValidade", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate dataValidade,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            Authentication authentication
    ) {
        return propostaService.criarPropostaComArquivo(
                idEmpresa, servicoId, valor, observacoes, dataValidade, arquivo, authentication
        );
    }

    @PostMapping("/comercial")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PROPOSTA_CRIAR')")
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
    @PreAuthorize("hasRole('EMPRESA')")
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
