package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.AuditoriaEvento;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.AuditoriaEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaEventoRepository auditoriaEventoRepository;

    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setIdUsuario(1);
        usuarioLogado.setEmail("joao@climbe.com");
        usuarioLogado.setNomeCompleto("João Silva");
    }

    @Test
    void registrarEvento_deveSalvarEventoComUsuarioLogado() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(usuarioLogado);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("chave", "valor");

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.CONTRATO_CRIADO, "CONTRATO", 100, payload);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getTipoEvento()).isEqualTo(TipoEventoAuditoria.CONTRATO_CRIADO);
        assertThat(eventoSalvo.getEntidade()).isEqualTo("CONTRATO");
        assertThat(eventoSalvo.getEntidadeId()).isEqualTo(100);
        assertThat(eventoSalvo.getAtorUsuarioId()).isEqualTo(1);
        assertThat(eventoSalvo.getAtorEmail()).isEqualTo("joao@climbe.com");
        assertThat(eventoSalvo.getPayloadJson()).isNotNull();
        assertThat(eventoSalvo.getCriadoEm()).isNotNull();
    }

    @Test
    void registrarEvento_deveSalvarEventoSemUsuarioQuandoNaoAutenticado() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(null);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("chave", "valor");

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.CONTRATO_CRIADO, "CONTRATO", 100, payload);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getAtorUsuarioId()).isNull();
        assertThat(eventoSalvo.getAtorEmail()).isNull();
    }

    @Test
    void registrarEvento_deveGerarCorrelationIdQuandoNaoFornecido() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(usuarioLogado);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.TAREFA_CRIADA, "TAREFA", 200, null);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getCorrelationId()).isNotNull();
        assertThat(eventoSalvo.getCorrelationId()).isNotEmpty();
    }

    @Test
    void registrarEvento_deveUsarCorrelationIdFornecido() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(usuarioLogado);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String correlationIdEsperado = "corr-12345";

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.TAREFA_CRIADA, "TAREFA", 200, null, correlationIdEsperado);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getCorrelationId()).isEqualTo(correlationIdEsperado);
    }

    @Test
    void registrarEvento_deveConverterPayloadParaJson() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(usuarioLogado);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 123);
        payload.put("nome", "Teste");
        payload.put("ativo", true);

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.USUARIO_CRIADO, "USUARIO", 1, payload);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getPayloadJson()).contains("\"id\":123");
        assertThat(eventoSalvo.getPayloadJson()).contains("\"nome\":\"Teste\"");
        assertThat(eventoSalvo.getPayloadJson()).contains("\"ativo\":true");
    }

    @Test
    void registrarEvento_deveUsarPayloadVazioQuandoNulo() {
        // Arrange
        when(usuarioLogadoService.obterUsuarioLogadoOrNull()).thenReturn(usuarioLogado);
        when(auditoriaEventoRepository.save(any(AuditoriaEvento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditoriaService.registrarEvento(TipoEventoAuditoria.LOGIN_REALIZADO, "USUARIO", 1, null);

        // Assert
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaEventoRepository).save(captor.capture());
        
        AuditoriaEvento eventoSalvo = captor.getValue();
        assertThat(eventoSalvo.getPayloadJson()).isEqualTo("{}");
    }
}
