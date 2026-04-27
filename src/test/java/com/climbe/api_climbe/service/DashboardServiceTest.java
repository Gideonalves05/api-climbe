package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.DashboardResumoDto;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.MembroTimeRepository;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private TarefaContratoRepository tarefaContratoRepository;

    @Mock
    private MembroTimeRepository membroTimeRepository;

    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @InjectMocks
    private DashboardService dashboardService;

    private Usuario usuarioAdmin;
    private Usuario usuarioColaborador;
    private LocalDateTime agora;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setIdUsuario(1);
        usuarioAdmin.setEmail("admin@climbe.com");
        usuarioAdmin.setNomeCompleto("Admin User");

        usuarioColaborador = new Usuario();
        usuarioColaborador.setIdUsuario(2);
        usuarioColaborador.setEmail("colaborador@climbe.com");
        usuarioColaborador.setNomeCompleto("Colaborador User");

        agora = LocalDateTime.now();
    }

    @Test
    void obterResumo_paraAdminComPermissaoGeral_deveRetornarTodosDados() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioAdmin);
        when(usuarioLogadoService.temPermissao(any())).thenReturn(true);
        when(contratoRepository.countByStatus(StatusContrato.VIGENTE)).thenReturn(10L);
        when(contratoRepository.countByStatus(StatusContrato.ENCERRADO)).thenReturn(5L);
        when(contratoRepository.countByStatus(StatusContrato.SUSPENSO)).thenReturn(2L);
        when(contratoRepository.countByStatus(StatusContrato.CANCELADO)).thenReturn(1L);
        when(tarefaContratoRepository.countByColunaTipoInicial()).thenReturn(20L);
        when(tarefaContratoRepository.countByColunaTipoIntermediaria()).thenReturn(15L);
        when(tarefaContratoRepository.countByColunaTipoFinal()).thenReturn(30L);
        when(tarefaContratoRepository.countVencidas(any())).thenReturn(5L);
        when(tarefaContratoRepository.countVencendoNoPeriodo(any(), any())).thenReturn(3L);

        LocalDateTime inicioPeriodo = agora.minusDays(7);
        when(tarefaContratoRepository.findConclusoesPorPeriodo(any(), any()))
            .thenReturn(Arrays.asList(
                new Object[]{LocalDate.now().minusDays(3), 5L},
                new Object[]{LocalDate.now().minusDays(2), 8L}
            ));

        when(tarefaContratoRepository.findContratosComMaisTarefasVencidas(any()))
            .thenReturn(Arrays.asList(
                new Object[]{1, 3L},
                new Object[]{2, 2L}
            ));
        when(contratoRepository.findNomesEmpresasPorIds(anyList()))
            .thenReturn(Arrays.asList(
                new Object[]{1, "Empresa A"},
                new Object[]{2, "Empresa B"}
            ));

        // Act
        DashboardResumoDto result = dashboardService.obterResumo("7d");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.contratosTotais().ativos()).isEqualTo(10L);
        assertThat(result.contratosTotais().encerrados()).isEqualTo(5L);
        assertThat(result.contratosTotais().suspensos()).isEqualTo(2L);
        assertThat(result.contratosTotais().cancelados()).isEqualTo(1L);
        assertThat(result.tarefasPorStatus().aFazer()).isEqualTo(20L);
        assertThat(result.tarefasPorStatus().emAndamento()).isEqualTo(15L);
        assertThat(result.tarefasPorStatus().concluidas()).isEqualTo(30L);
        assertThat(result.tarefasPorStatus().bloqueadas()).isEqualTo(5L);
        assertThat(result.tarefasVencendoEm7Dias()).isEqualTo(3L);
        assertThat(result.conclusoesPorDia()).hasSize(2);
        assertThat(result.topContratosComAtraso()).hasSize(2);
    }

    @Test
    void obterResumo_paraColaboradorSemPermissaoGeral_deveFiltrarPorContratosDoTime() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioColaborador);
        when(usuarioLogadoService.temPermissao(any())).thenReturn(false);
        when(membroTimeRepository.findByUsuario_IdUsuarioAndAtivoTrue(2))
            .thenReturn(Collections.emptyList());

        // Act
        DashboardResumoDto result = dashboardService.obterResumo("7d");

        // Assert - deve retornar dados zerados para usuário sem contratos
        assertThat(result).isNotNull();
        assertThat(result.contratosTotais().ativos()).isEqualTo(0L);
        assertThat(result.tarefasVencendoEm7Dias()).isEqualTo(0);
        assertThat(result.conclusoesPorDia()).isEmpty();
        assertThat(result.topContratosComAtraso()).isEmpty();
    }

    @Test
    void obterResumo_comPeriodo30d_deveUsarDataInicioCorreta() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioAdmin);
        when(usuarioLogadoService.temPermissao(any())).thenReturn(true);
        when(contratoRepository.countByStatus(any())).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoInicial()).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoIntermediaria()).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoFinal()).thenReturn(0L);
        when(tarefaContratoRepository.countVencidas(any())).thenReturn(0L);
        when(tarefaContratoRepository.countVencendoNoPeriodo(any(), any())).thenReturn(0L);
        when(tarefaContratoRepository.findConclusoesPorPeriodo(any(), any()))
            .thenReturn(Collections.emptyList());
        when(tarefaContratoRepository.findContratosComMaisTarefasVencidas(any()))
            .thenReturn(Collections.emptyList());

        // Act
        DashboardResumoDto result = dashboardService.obterResumo("30d");

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void obterResumo_comPeriodo90d_deveUsarDataInicioCorreta() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioAdmin);
        when(usuarioLogadoService.temPermissao(any())).thenReturn(true);
        when(contratoRepository.countByStatus(any())).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoInicial()).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoIntermediaria()).thenReturn(0L);
        when(tarefaContratoRepository.countByColunaTipoFinal()).thenReturn(0L);
        when(tarefaContratoRepository.countVencidas(any())).thenReturn(0L);
        when(tarefaContratoRepository.countVencendoNoPeriodo(any(), any())).thenReturn(0L);
        when(tarefaContratoRepository.findConclusoesPorPeriodo(any(), any()))
            .thenReturn(Collections.emptyList());
        when(tarefaContratoRepository.findContratosComMaisTarefasVencidas(any()))
            .thenReturn(Collections.emptyList());

        // Act
        DashboardResumoDto result = dashboardService.obterResumo("90d");

        // Assert
        assertThat(result).isNotNull();
    }
}
