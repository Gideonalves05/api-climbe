package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.ColunaKanban;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoColuna;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.ColunaKanbanRepository;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColunaKanbanServiceTest {

    @Mock
    private ColunaKanbanRepository colunaKanbanRepository;

    @Mock
    private TarefaContratoRepository tarefaContratoRepository;

    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ColunaKanbanService colunaKanbanService;

    private Contrato contrato;
    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setIdContrato(1);

        usuarioLogado = new Usuario();
        usuarioLogado.setIdUsuario(1);
        usuarioLogado.setNome("Teste");
    }

    @Test
    @DisplayName("provisionarPadrao deve criar 3 colunas quando contrato nao tem colunas")
    void testeProvisionarPadraoCriaTresColunas() {
        // Arrange
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(colunaKanbanRepository.countByContrato_IdContrato(1)).thenReturn(0L);
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        // Act
        List<ColunaKanban> resultado = colunaKanbanService.provisionarPadrao(1, true);

        // Assert
        assertThat(resultado).hasSize(3);

        ArgumentCaptor<ColunaKanban> colunaCaptor = ArgumentCaptor.forClass(ColunaKanban.class);
        verify(colunaKanbanRepository, times(3)).save(colunaCaptor.capture());

        List<ColunaKanban> colunasSalvas = colunaCaptor.getAllValues();

        // Verificar ordem e tipos
        assertThat(colunasSalvas.get(0).getNome()).isEqualTo("A Fazer");
        assertThat(colunasSalvas.get(0).getOrdem()).isEqualTo(1);
        assertThat(colunasSalvas.get(0).getTipo()).isEqualTo(TipoColuna.INICIAL);
        assertThat(colunasSalvas.get(0).getCor()).isEqualTo("#f59e0b");

        assertThat(colunasSalvas.get(1).getNome()).isEqualTo("Em Andamento");
        assertThat(colunasSalvas.get(1).getOrdem()).isEqualTo(2);
        assertThat(colunasSalvas.get(1).getTipo()).isEqualTo(TipoColuna.INTERMEDIARIA);
        assertThat(colunasSalvas.get(1).getCor()).isEqualTo("#3b82f6");

        assertThat(colunasSalvas.get(2).getNome()).isEqualTo("Concluído");
        assertThat(colunasSalvas.get(2).getOrdem()).isEqualTo(3);
        assertThat(colunasSalvas.get(2).getTipo()).isEqualTo(TipoColuna.FINAL);
        assertThat(colunasSalvas.get(2).getCor()).isEqualTo("#10b981");
    }

    @Test
    @DisplayName("provisionarPadrao deve ser idempotente - nao criar duplicatas")
    void testeProvisionarPadraoIdempotente() {
        // Arrange - contrato ja tem colunas
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(colunaKanbanRepository.countByContrato_IdContrato(1)).thenReturn(3L);

        // Act - chamar duas vezes
        List<ColunaKanban> resultado1 = colunaKanbanService.provisionarPadrao(1, false);
        List<ColunaKanban> resultado2 = colunaKanbanService.provisionarPadrao(1, false);

        // Assert - nenhuma coluna foi salva
        assertThat(resultado1).isEmpty();
        assertThat(resultado2).isEmpty();
        verify(colunaKanbanRepository, never()).save(any(ColunaKanban.class));
    }

    @Test
    @DisplayName("provisionarPadrao deve registrar auditoria quando solicitado")
    void testeProvisionarPadraoRegistraAuditoria() {
        // Arrange
        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(colunaKanbanRepository.countByContrato_IdContrato(1)).thenReturn(0L);
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        // Act
        colunaKanbanService.provisionarPadrao(1, true);

        // Assert
        verify(auditoriaService).registrarEvento(
            eq(TipoEventoAuditoria.KANBAN_INICIALIZADO),
            eq("CONTRATO"),
            eq(1),
            argThat(payload -> payload != null && payload.containsKey("colunasCriadas")),
            any()
        );
    }

    @Test
    @DisplayName("provisionarPadrao deve retornar colunas existentes sem criar novas")
    void testeProvisionarPadraoRetornaColunasExistentes() {
        // Arrange - contrato ja tem 3 colunas
        ColunaKanban coluna1 = new ColunaKanban();
        coluna1.setIdColuna(1);
        coluna1.setNome("A Fazer");

        when(contratoRepository.findById(1)).thenReturn(Optional.of(contrato));
        when(colunaKanbanRepository.countByContrato_IdContrato(1)).thenReturn(3L);
        when(colunaKanbanRepository.findByContrato_IdContratoOrderByOrdemAsc(1))
            .thenReturn(List.of(coluna1));

        // Act
        List<ColunaKanban> resultado = colunaKanbanService.provisionarPadrao(1, false);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("A Fazer");
        verify(colunaKanbanRepository, never()).save(any());
    }
}
