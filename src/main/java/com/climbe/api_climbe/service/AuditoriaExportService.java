package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AuditoriaEventoDto;
import com.climbe.api_climbe.dto.FiltroAuditoriaDto;
import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.repository.AuditoriaEventoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaExportService {

    private final AuditoriaEventoRepository auditoriaEventoRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportarCsv(FiltroAuditoriaDto filtro) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "criadoEm"));
            Page<AuditoriaEvento> eventos = buscarEventosComFiltro(filtro, pageable);
            
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Tipo Evento,Entidade,Entidade ID,Ator Usuario ID,Ator Email,Correlation ID,Criado Em,Payload\n");
            
            for (AuditoriaEvento evento : eventos.getContent()) {
                csv.append(formatarLinhaCsv(evento)).append("\n");
            }
            
            outputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
            log.info("Exportação CSV de auditoria realizada: {} registros", eventos.getTotalElements());
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Erro ao gerar CSV de auditoria", e);
            throw new RuntimeException("Erro ao gerar CSV de auditoria", e);
        }
    }

    public byte[] exportarPdf(FiltroAuditoriaDto filtro) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "criadoEm"));
            Page<AuditoriaEvento> eventos = buscarEventosComFiltro(filtro, pageable);
            
            StringBuilder pdf = new StringBuilder();
            pdf.append("Relatório de Auditoria\n");
            pdf.append("====================\n\n");
            pdf.append("Total de registros: ").append(eventos.getTotalElements()).append("\n\n");
            
            for (AuditoriaEvento evento : eventos.getContent()) {
                pdf.append(formatarLinhaPdf(evento)).append("\n");
                pdf.append("----------------------------------------\n");
            }
            
            outputStream.write(pdf.toString().getBytes(StandardCharsets.UTF_8));
            log.info("Exportação PDF de auditoria realizada: {} registros", eventos.getTotalElements());
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Erro ao gerar PDF de auditoria", e);
            throw new RuntimeException("Erro ao gerar PDF de auditoria", e);
        }
    }

    private Page<AuditoriaEvento> buscarEventosComFiltro(FiltroAuditoriaDto filtro, Pageable pageable) {
        Integer pagina = filtro.pagina() != null ? filtro.pagina() : 0;
        Integer tamanho = filtro.tamanho() != null ? filtro.tamanho() : 20;
        Pageable paginacao = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"));
        
        return auditoriaEventoRepository.buscarComFiltros(
            filtro.tipoEvento(),
            filtro.entidade(),
            filtro.entidadeId(),
            filtro.atorUsuarioId(),
            filtro.dataInicio(),
            filtro.dataFim(),
            paginacao
        );
    }

    private String formatarLinhaCsv(AuditoriaEvento evento) {
        StringBuilder linha = new StringBuilder();
        linha.append(escaparCsv(evento.getId().toString())).append(",");
        linha.append(escaparCsv(evento.getTipoEvento() != null ? evento.getTipoEvento().name() : "")).append(",");
        linha.append(escaparCsv(evento.getEntidade() != null ? evento.getEntidade() : "")).append(",");
        linha.append(escaparCsv(evento.getEntidadeId() != null ? evento.getEntidadeId().toString() : "")).append(",");
        linha.append(escaparCsv(evento.getAtorUsuarioId() != null ? evento.getAtorUsuarioId().toString() : "")).append(",");
        linha.append(escaparCsv(evento.getAtorEmail() != null ? evento.getAtorEmail() : "")).append(",");
        linha.append(escaparCsv(evento.getCorrelationId() != null ? evento.getCorrelationId() : "")).append(",");
        linha.append(escaparCsv(evento.getCriadoEm() != null ? evento.getCriadoEm().format(DATE_FORMATTER) : "")).append(",");
        linha.append(escaparCsv(evento.getPayloadJson() != null ? evento.getPayloadJson() : ""));
        return linha.toString();
    }

    private String formatarLinhaPdf(AuditoriaEvento evento) {
        StringBuilder linha = new StringBuilder();
        linha.append("ID: ").append(evento.getId()).append("\n");
        linha.append("Tipo: ").append(evento.getTipoEvento() != null ? evento.getTipoEvento().name() : "").append("\n");
        linha.append("Entidade: ").append(evento.getEntidade() != null ? evento.getEntidade() : "").append("\n");
        linha.append("Entidade ID: ").append(evento.getEntidadeId() != null ? evento.getEntidadeId() : "").append("\n");
        linha.append("Ator Usuario ID: ").append(evento.getAtorUsuarioId() != null ? evento.getAtorUsuarioId() : "").append("\n");
        linha.append("Ator Email: ").append(evento.getAtorEmail() != null ? evento.getAtorEmail() : "").append("\n");
        linha.append("Correlation ID: ").append(evento.getCorrelationId() != null ? evento.getCorrelationId() : "").append("\n");
        linha.append("Criado Em: ").append(evento.getCriadoEm() != null ? evento.getCriadoEm().format(DATE_FORMATTER) : "").append("\n");
        linha.append("Payload: ").append(evento.getPayloadJson() != null ? evento.getPayloadJson() : "").append("\n");
        return linha.toString();
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
