package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Notificacao;
import com.climbe.api_climbe.model.NotificacaoOutbox;
import com.climbe.api_climbe.model.PreferenciaNotificacao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CanalNotificacao;
import com.climbe.api_climbe.model.enums.StatusEntregaNotificacao;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.NotificacaoOutboxRepository;
import com.climbe.api_climbe.repository.NotificacaoRepository;
import com.climbe.api_climbe.repository.PreferenciaNotificacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoPublisherImplTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private NotificacaoOutboxRepository outboxRepository;

    @Mock
    private PreferenciaNotificacaoRepository preferenciaRepository;

    @Mock
    private SseService sseService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificacaoPublisherImpl publisher;

    private Usuario usuario1;
    private Usuario usuario2;

    @BeforeEach
    void setUp() {
        usuario1 = new Usuario();
        usuario1.setIdUsuario(1);
        usuario1.setEmail("joao@climbe.com");

        usuario2 = new Usuario();
        usuario2.setIdUsuario(2);
        usuario2.setEmail("maria@climbe.com");
    }

    @Test
    void publicar_deveCriarNotificacaoEOutboxParaCadaDestinatario() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1, usuario2);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        String titulo = "Nova tarefa atribuída";
        String mensagem = "Você foi designado para uma tarefa";
        String linkDestino = "/contratos/12/tarefas/345";
        Map<String, Object> payload = Map.of("contratoId", 12, "tarefaId", 345);

        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, titulo, mensagem, linkDestino, payload);

        // Assert
        verify(notificacaoRepository, times(2)).save(any(Notificacao.class));
        // Cada destinatário gera 2 outbox (IN_APP + EMAIL quando sem opt-out)
        verify(outboxRepository, times(4)).save(any(NotificacaoOutbox.class));
    }

    @Test
    void publicar_deveCriarOutboxInAppSempre() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, null);

        // Assert
        ArgumentCaptor<NotificacaoOutbox> outboxCaptor = ArgumentCaptor.forClass(NotificacaoOutbox.class);
        verify(outboxRepository, atLeastOnce()).save(outboxCaptor.capture());
        
        assertThat(outboxCaptor.getAllValues()).anyMatch(outbox -> 
            outbox.getCanal() == CanalNotificacao.IN_APP &&
            outbox.getDestino().equals("1") &&
            outbox.getStatus() == StatusEntregaNotificacao.PENDENTE
        );
    }

    @Test
    void publicar_deveCriarOutboxSseQuandoUsuarioConectado() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(1)).thenReturn(true);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, null);

        // Assert
        ArgumentCaptor<NotificacaoOutbox> outboxCaptor = ArgumentCaptor.forClass(NotificacaoOutbox.class);
        verify(outboxRepository, atLeastOnce()).save(outboxCaptor.capture());
        
        assertThat(outboxCaptor.getAllValues()).anyMatch(outbox -> 
            outbox.getCanal() == CanalNotificacao.SSE &&
            outbox.getDestino().equals("1")
        );
    }

    @Test
    void publicar_naoDeveCriarOutboxSseQuandoUsuarioDesconectado() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(1)).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, null);

        // Assert
        ArgumentCaptor<NotificacaoOutbox> outboxCaptor = ArgumentCaptor.forClass(NotificacaoOutbox.class);
        verify(outboxRepository, atLeastOnce()).save(outboxCaptor.capture());
        
        assertThat(outboxCaptor.getAllValues()).noneMatch(outbox -> outbox.getCanal() == CanalNotificacao.SSE);
    }

    @Test
    void publicar_deveCriarOutboxEmailQuandoNaoHouverOptOut() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(usuario1, tipo, CanalNotificacao.EMAIL))
            .thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, null);

        // Assert
        ArgumentCaptor<NotificacaoOutbox> outboxCaptor = ArgumentCaptor.forClass(NotificacaoOutbox.class);
        verify(outboxRepository, atLeastOnce()).save(outboxCaptor.capture());
        
        assertThat(outboxCaptor.getAllValues()).anyMatch(outbox -> 
            outbox.getCanal() == CanalNotificacao.EMAIL &&
            outbox.getDestino().equals("joao@climbe.com")
        );
    }

    @Test
    void publicar_naoDeveCriarOutboxEmailQuandoHouverOptOut() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        
        PreferenciaNotificacao pref = new PreferenciaNotificacao();
        pref.setHabilitado(false);
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(usuario1, tipo, CanalNotificacao.EMAIL))
            .thenReturn(Optional.of(pref));

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, null);

        // Assert
        ArgumentCaptor<NotificacaoOutbox> outboxCaptor = ArgumentCaptor.forClass(NotificacaoOutbox.class);
        verify(outboxRepository, atLeastOnce()).save(outboxCaptor.capture());
        
        assertThat(outboxCaptor.getAllValues()).noneMatch(outbox -> outbox.getCanal() == CanalNotificacao.EMAIL);
    }

    @Test
    void publicar_deveSerializarPayloadCorretamente() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        Map<String, Object> payload = Map.of("chave", "valor", "numero", 123);
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, "Título", "Mensagem", null, payload);

        // Assert
        ArgumentCaptor<Notificacao> notifCaptor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(notifCaptor.capture());
        
        assertThat(notifCaptor.getValue().getPayload()).isNotNull();
        assertThat(notifCaptor.getValue().getPayload()).contains("chave");
        assertThat(notifCaptor.getValue().getPayload()).contains("valor");
    }

    @Test
    void publicar_deveCriarNotificacaoComCamposCorretos() {
        // Arrange
        Set<Usuario> destinatarios = Set.of(usuario1);
        TipoNotificacao tipo = TipoNotificacao.TAREFA_CRIADA;
        String titulo = "Título Teste";
        String mensagem = "Mensagem Teste";
        String linkDestino = "/teste/link";
        Map<String, Object> payload = Map.of("key", "value");
        
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(NotificacaoOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sseService.usuarioConectado(anyInt())).thenReturn(false);
        when(preferenciaRepository.findByUsuarioAndTipoAndCanal(any(), any(), any())).thenReturn(Optional.empty());

        // Act
        publisher.publicar(destinatarios, tipo, titulo, mensagem, linkDestino, payload);

        // Assert
        ArgumentCaptor<Notificacao> notifCaptor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(notifCaptor.capture());
        
        Notificacao notif = notifCaptor.getValue();
        assertThat(notif.getUsuario()).isEqualTo(usuario1);
        assertThat(notif.getTipo()).isEqualTo(tipo);
        assertThat(notif.getTitulo()).isEqualTo(titulo);
        assertThat(notif.getMensagem()).isEqualTo(mensagem);
        assertThat(notif.getLinkDestino()).isEqualTo(linkDestino);
        assertThat(notif.getLida()).isFalse();
    }
}
