package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.FiltroAuditoriaDto;
import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.AuditoriaEventoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaExportServiceTest {

    @Mock
    private AuditoriaEventoRepository auditoriaEventoRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditoriaExportService auditoriaExportService;

    private AuditoriaEvento evento1;
    private AuditoriaEvento evento2;

    @BeforeEach
    void setUp() {
        evento1 = new AuditoriaEvento();
        evento1.setId(1);
        evento1.setTipoEvento(TipoEventoAuditoria.CONTRATO_CRIADO);
        evento1.setEntidade("CONTRATO");
        evento1.setEntidadeId(100);
        evento1.setAtorUsuarioId(1);
        evento1.setAtorEmail("joao@climbe.com");
        evento1.setCorrelationId("corr-123");
        evento1.setPayloadJson("{\"id\":100}");
        evento1.setCriadoEm(LocalDateTime.of(2024, 1, 1, 10, 0));

        evento2 = new AuditoriaEvento();
        evento2.setId(2);
        evento2.setTipoEvento(TipoEventoAuditoria.TAREFA_CRIADA);
        evento2.setEntidade("TAREFA");
        evento2.setEntidadeId(200);
        evento2.setAtorUsuarioId(1);
        evento2.setAtorEmail("joao@climbe.com");
        evento2.setCorrelationId("corr-456");
        evento2.setPayloadJson("{\"id\":200}");
        evento2.setCriadoEm(LocalDateTime.of(2024, 1, 2, 11, 0));
    }

    @Test
    void exportarCsv_deveGerarCsvComEventos() {
        // Arrange
        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(null, null, null, null, null, null, 0, 20);
        Page<AuditoriaEvento> page = new PageImpl<>(List.of(evento1, evento2));
        when(auditoriaEventoRepository.buscarComFiltros(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        byte[] csv = auditoriaExportService.exportarCsv(filtro);

        // Assert
        assertThat(csv).isNotNull();
        String csvString = new String(csv);
        assertThat(csvString).contains("ID,Tipo Evento,Entidade,Entidade ID,Ator Usuario ID,Ator Email,Correlation ID,Criado Em,Payload");
        assertThat(csvString).contains("1,CONTRATO_CRIADO,CONTRATO,100,1,joao@climbe.com,corr-123");
        assertThat(csvString).contains("2,TAREFA_CRIADA,TAREFA,200,1,joao@climbe.com,corr-456");
    }

    @Test
    void exportarPdf_deveGerarPdfComEventos() {
        // Arrange
        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(null, null, null, null, null, null, 0, 20);
        Page<AuditoriaEvento> page = new PageImpl<>(List.of(evento1, evento2));
        when(auditoriaEventoRepository.buscarComFiltros(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        byte[] pdf = auditoriaExportService.exportarPdf(filtro);

        // Assert
        assertThat(pdf).isNotNull();
        String pdfString = new String(pdf);
        assertThat(pdfString).contains("Relatório de Auditoria");
        assertThat(pdfString).contains("Total de registros: 2");
        assertThat(pdfString).contains("ID: 1");
        assertThat(pdfString).contains("Tipo: CONTRATO_CRIADO");
        assertThat(pdfString).contains("ID: 2");
        assertThat(pdfString).contains("Tipo: TAREFA_CRIADA");
    }

    @Test
    void exportarCsv_deveEscaparCaracteresEspeciais() {
        // Arrange
        AuditoriaEvento eventoComVirgula = new AuditoriaEvento();
        eventoComVirgula.setId(3);
        eventoComVirgula.setTipoEvento(TipoEventoAuditoria.USUARIO_CRIADO);
        eventoComVirgula.setEntidade("USUARIO");
        eventoComVirgula.setEntidadeId(3);
        eventoComVirgula.setAtorEmail("joao, silva@climbe.com");
        eventoComVirgula.setPayloadJson("{\"nome\":\"João, Silva\"}");
        eventoComVirgula.setCriadoEm(LocalDateTime.now());

        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(null, null, null, null, null, null, 0, 20);
        Page<AuditoriaEvento> page = new PageImpl<>(List.of(eventoComVirgula));
        when(auditoriaEventoRepository.buscarComFiltros(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        byte[] csv = auditoriaExportService.exportarCsv(filtro);

        // Assert
        assertThat(csv).isNotNull();
        String csvString = new String(csv);
        assertThat(csvString).contains("\"joao, silva@climbe.com\"");
        assertThat(csvString).contains("\"João, Silva\"");
    }

    @Test
    void exportarCsv_comFiltroPorTipoEvento() {
        // Arrange
        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(
                TipoEventoAuditoria.CONTRATO_CRIADO,
                null, null, null, null, null, 0, 20
        );
        Page<AuditoriaEvento> page = new PageImpl<>(List.of(evento1));
        when(auditoriaEventoRepository.buscarComFiltros(
                eq(TipoEventoAuditoria.CONTRATO_CRIADO),
                any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        byte[] csv = auditoriaExportService.exportarCsv(filtro);

        // Assert
        assertThat(csv).isNotNull();
        String csvString = new String(csv);
        assertThat(csvString).contains("CONTRATO_CRIADO");
    }

    @Test
    void exportarPdf_comFiltroPorEntidade() {
        // Arrange
        FiltroAuditoriaDto filtro = new FiltroAuditoriaDto(
                null, "TAREFA", null, null, null, null, 0, 20
        );
        Page<AuditoriaEvento> page = new PageImpl<>(List.of(evento2));
        when(auditoriaEventoRepository.buscarComFiltros(
                any(),
                eq("TAREFA"),
                any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        byte[] pdf = auditoriaExportService.exportarPdf(filtro);

        // Assert
        assertThat(pdf).isNotNull();
        String pdfString = new String(pdf);
        assertThat(pdfString).contains("Entidade: TAREFA");
    }
}
