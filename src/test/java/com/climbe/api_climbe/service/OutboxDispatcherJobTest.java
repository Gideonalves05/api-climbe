package com.climbe.api_climbe.service;

import com.climbe.api_climbe.config.PropriedadesNotificacao;
import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.NotificacaoOutboxRepository;
import com.climbe.api_climbe.repository.NotificacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxDispatcherJobTest {

    @Mock
    private NotificacaoOutboxRepository outboxRepository;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private SseService sseService;

    @Mock
    private GmailApiClient gmailApiClient;

    @Mock
    private EmailBodyBuilder emailBodyBuilder;

    @Mock
    private PropriedadesNotificacao propriedades;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxDispatcherJob job;

    private NotificacaoOutbox outboxInApp;
    private NotificacaoOutbox outboxSse;
    private NotificacaoOutbox outboxEmail;
    private Notificacao notificacao;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setEmail("joao@climbe.com");

        notificacao = new Notificacao();
        notificacao.setIdNotificacao(100);
        notificacao.setUsuario(usuario);
        notificacao.setTipo(TipoNotificacao.TAREFA_CRIADA);
        notificacao.setTitulo("Título");
        notificacao.setMensagem("Mensagem");
        notificacao.setLinkDestino("/link");
        notificacao.setPayload("{}");
        notificacao.setLida(false);
        notificacao.setCriadoEm(LocalDateTime.now());

        outboxInApp = new NotificacaoOutbox();
        outboxInApp.setIdOutbox(1L);
        outboxInApp.setNotificacao(notificacao);
        outboxInApp.setCanal(CanalNotificacao.IN_APP);
        outboxInApp.setDestino("1");
        outboxInApp.setStatus(StatusEntregaNotificacao.PENDENTE);
        outboxInApp.setTentativas(0);
        outboxInApp.setProximaTentativa(LocalDateTime.now().minusMinutes(1));
        outboxInApp.setCriadoEm(LocalDateTime.now());
        outboxInApp.setAtualizadoEm(LocalDateTime.now());

        outboxSse = new NotificacaoOutbox();
        outboxSse.setIdOutbox(2L);
        outboxSse.setNotificacao(notificacao);
        outboxSse.setCanal(CanalNotificacao.SSE);
        outboxSse.setDestino("1");
        outboxSse.setStatus(StatusEntregaNotificacao.PENDENTE);
        outboxSse.setTentativas(0);
        outboxSse.setProximaTentativa(LocalDateTime.now().minusMinutes(1));
        outboxSse.setCriadoEm(LocalDateTime.now());
        outboxSse.setAtualizadoEm(LocalDateTime.now());

        outboxEmail = new NotificacaoOutbox();
        outboxEmail.setIdOutbox(3L);
        outboxEmail.setNotificacao(notificacao);
        outboxEmail.setCanal(CanalNotificacao.EMAIL);
        outboxEmail.setDestino("joao@climbe.com");
        outboxEmail.setStatus(StatusEntregaNotificacao.PENDENTE);
        outboxEmail.setTentativas(0);
        outboxEmail.setProximaTentativa(LocalDateTime.now().minusMinutes(1));
        outboxEmail.setCriadoEm(LocalDateTime.now());
        outboxEmail.setAtualizadoEm(LocalDateTime.now());

        lenient().when(propriedades.getBatchSize()).thenReturn(100);
        lenient().when(propriedades.getDelaysRetry()).thenReturn(List.of(
            Duration.ofSeconds(30),
            Duration.ofMinutes(2),
            Duration.ofMinutes(10),
            Duration.ofHours(1),
            Duration.ofHours(6),
            Duration.ofHours(24)
        ));
    }

    @Test
    void processarPendentes_naoDeveFazerNadaQuandoNaoHouverPendentes() {
        // Arrange
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of());

        // Act
        job.processarPendentes();

        // Assert
        verify(outboxRepository).findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class));
        verify(outboxRepository, never()).save(any(NotificacaoOutbox.class));
    }

    @Test
    void processarPendentes_deveProcessarOutboxInApp() {
        // Arrange
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(outboxInApp));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        job.processarPendentes();

        // Assert
        verify(outboxRepository).save(any(NotificacaoOutbox.class));
        assertThat(outboxInApp.getStatus()).isEqualTo(StatusEntregaNotificacao.ENVIADA);
        assertThat(outboxInApp.getUltimaTentativaEm()).isNotNull();
    }

    @Test
    void processarPendentes_deveProcessarOutboxSse() throws Exception {
        // Arrange
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(outboxSse));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(Map.of());

        // Act
        job.processarPendentes();

        // Assert
        verify(sseService).enviarParaUsuario(eq(1), eq("notificacao"), any(Map.class));
        verify(outboxRepository).save(any(NotificacaoOutbox.class));
        assertThat(outboxSse.getStatus()).isEqualTo(StatusEntregaNotificacao.ENVIADA);
    }

    @Test
    void processarPendentes_deveProcessarOutboxEmail() throws Exception {
        // Arrange
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(outboxEmail));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailBodyBuilder.construir(any(), any(), any(), any(), any())).thenReturn("<html>corpo</html>");
        when(gmailApiClient.enviarEmail(any(), any(), any(), any())).thenReturn(true);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(Map.of());

        // Act
        job.processarPendentes();

        // Assert
        verify(emailBodyBuilder).construir(any(), any(), any(), any(), any());
        verify(gmailApiClient).enviarEmail(eq("joao@climbe.com"), any(), any(), any());
        verify(outboxRepository).save(any(NotificacaoOutbox.class));
        assertThat(outboxEmail.getStatus()).isEqualTo(StatusEntregaNotificacao.ENVIADA);
    }

    @Test
    void processarPendentes_deveAgendarRetryQuandoFalhar() {
        // Arrange
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(outboxEmail));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailBodyBuilder.construir(any(), any(), any(), any(), any())).thenReturn("<html>corpo</html>");
        when(gmailApiClient.enviarEmail(any(), any(), any(), any())).thenThrow(new RuntimeException("Falha no envio"));

        // Act
        job.processarPendentes();

        // Assert
        verify(outboxRepository).save(any(NotificacaoOutbox.class));
        assertThat(outboxEmail.getTentativas()).isEqualTo(1);
        assertThat(outboxEmail.getStatus()).isEqualTo(StatusEntregaNotificacao.PENDENTE);
        assertThat(outboxEmail.getProximaTentativa()).isAfter(LocalDateTime.now());
        assertThat(outboxEmail.getUltimoErro()).isNotNull();
    }

    @Test
    void processarPendentes_deveMarcarErroPermanenteAposMaxTentativas() throws Exception {
        // Arrange
        outboxEmail.setTentativas(5); // Já tem 5 tentativas
        outboxEmail.setMaxTentativas(6);
        
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(List.of(outboxEmail));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailBodyBuilder.construir(any(), any(), any(), any(), any())).thenReturn("<html>corpo</html>");
        when(gmailApiClient.enviarEmail(any(), any(), any(), any())).thenThrow(new RuntimeException("Falha no envio"));

        // Act
        job.processarPendentes();

        // Assert
        verify(outboxRepository).save(any(NotificacaoOutbox.class));
        assertThat(outboxEmail.getTentativas()).isEqualTo(6);
        assertThat(outboxEmail.getStatus()).isEqualTo(StatusEntregaNotificacao.ERRO_PERMANENTE);
        assertThat(outboxEmail.getUltimoErro()).isNotNull();
    }

    @Test
    void processarPendentes_deveRespeitarBatchSize() throws Exception {
        // Arrange
        List<NotificacaoOutbox> pendentes = List.of(outboxInApp, outboxSse, outboxEmail);
        when(outboxRepository.findPendentesParaProcessar(eq(StatusEntregaNotificacao.PENDENTE), any(LocalDateTime.class)))
            .thenReturn(pendentes);
        when(propriedades.getBatchSize()).thenReturn(2);
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(Map.of());
        lenient().when(emailBodyBuilder.construir(any(), any(), any(), any(), any())).thenReturn("<html>corpo</html>");
        lenient().when(gmailApiClient.enviarEmail(any(), any(), any(), any())).thenReturn(true);

        // Act
        job.processarPendentes();

        // Assert
        verify(outboxRepository, times(2)).save(any(NotificacaoOutbox.class)); // Apenas 2 do batch
    }
}
