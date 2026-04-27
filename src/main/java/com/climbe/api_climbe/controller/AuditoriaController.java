package com.climbe.api_climbe.controller;

import com.climbe.api_climbe.dto.AuditoriaEventoDto;
import com.climbe.api_climbe.dto.FiltroAuditoriaDto;
import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.repository.AuditoriaEventoRepository;
import com.climbe.api_climbe.service.AuditoriaExportService;
import com.climbe.api_climbe.service.UsuarioLogadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "API de consulta e exportação de trilha de auditoria")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditoriaEventoRepository auditoriaEventoRepository;
    private final AuditoriaExportService auditoriaExportService;
    private final UsuarioLogadoService usuarioLogadoService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_VER')")
    @Operation(summary = "Consultar eventos de auditoria com filtros")
    public ResponseEntity<Page<AuditoriaEventoDto>> consultarAuditoria(
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) Integer atorUsuarioId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanho) {

        usuarioLogadoService.exigirPermissao(CodigoPermissao.AUDITORIA_VER);

        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(
                parseTipoEvento(tipoEvento),
                entidade,
                entidadeId,
                atorUsuarioId,
                dataInicio,
                dataFim,
                pagina,
                tamanho
        );

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"));
        Page<AuditoriaEvento> eventos = auditoriaEventoRepository.buscarComFiltros(
                filtro.tipoEvento(),
                filtro.entidade(),
                filtro.entidadeId(),
                filtro.atorUsuarioId(),
                filtro.dataInicio(),
                filtro.dataFim(),
                pageable
        );

        Page<AuditoriaEventoDto> dtos = eventos.map(this::paraDto);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/export.csv")
    @PreAuthorize("hasAuthority('AUDITORIA_EXPORTAR')")
    @Operation(summary = "Exportar eventos de auditoria em CSV")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) Integer atorUsuarioId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim) {

        usuarioLogadoService.exigirPermissao(CodigoPermissao.AUDITORIA_EXPORTAR);

        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(
                parseTipoEvento(tipoEvento),
                entidade,
                entidadeId,
                atorUsuarioId,
                dataInicio,
                dataFim,
                null,
                null
        );

        byte[] csv = auditoriaExportService.exportarCsv(filtro);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "auditoria_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }

    @GetMapping("/export.pdf")
    @PreAuthorize("hasAuthority('AUDITORIA_EXPORTAR')")
    @Operation(summary = "Exportar eventos de auditoria em PDF")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) Integer entidadeId,
            @RequestParam(required = false) Integer atorUsuarioId,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim) {

        usuarioLogadoService.exigirPermissao(CodigoPermissao.AUDITORIA_EXPORTAR);

        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(
                parseTipoEvento(tipoEvento),
                entidade,
                entidadeId,
                atorUsuarioId,
                dataInicio,
                dataFim,
                null,
                null
        );

        byte[] pdf = auditoriaExportService.exportarPdf(filtro);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "auditoria_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    private com.climbe.api_climbe.model.enums.TipoEventoAuditoria parseTipoEvento(String tipoEvento) {
        if (tipoEvento == null || tipoEvento.isBlank()) {
            return null;
        }
        try {
            return com.climbe.api_climbe.model.enums.TipoEventoAuditoria.valueOf(tipoEvento);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private AuditoriaEventoDto paraDto(AuditoriaEvento evento) {
        return new AuditoriaEventoDto(
                evento.getId(),
                evento.getTipoEvento(),
                evento.getEntidade(),
                evento.getEntidadeId(),
                evento.getAtorUsuarioId(),
                evento.getAtorEmail(),
                evento.getCorrelationId(),
                evento.getPayloadJson(),
                evento.getCriadoEm()
        );
    }
}
