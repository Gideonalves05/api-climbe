package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.*;
import com.climbe.api_climbe.model.enums.PapelTime;
import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import com.climbe.api_climbe.model.enums.TipoColuna;
import com.climbe.api_climbe.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaContratoServiceTest {

    @Mock
    private TarefaContratoRepository tarefaContratoRepository;

    @Mock
    private ColunaKanbanRepository colunaKanbanRepository;

    @Mock
    private MembroTimeRepository membroTimeRepository;

    @Mock
    private TarefaDependenciaRepository tarefaDependenciaRepository;

    @Mock
    private UsuarioLogadoService usuarioLogadoService;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private TarefaContratoService tarefaContratoService;

    private Usuario usuarioLogado;
    private Contrato contrato;
    private ColunaKanban colunaInicial;
    private Usuario responsavel;
    private MembroTime membroTime;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setIdUsuario(1);
        usuarioLogado.setNomeCompleto("João Silva");
        usuarioLogado.setEmail("joao.silva@climbe.com");

        contrato = new Contrato();
        contrato.setIdContrato(1);

        colunaInicial = new ColunaKanban();
        colunaInicial.setIdColuna(1);
        colunaInicial.setContrato(contrato);
        colunaInicial.setNome("A Fazer");
        colunaInicial.setOrdem(1);
        colunaInicial.setTipo(TipoColuna.INICIAL);

        responsavel = new Usuario();
        responsavel.setIdUsuario(2);
        responsavel.setNomeCompleto("Maria Santos");
        responsavel.setEmail("maria.santos@climbe.com");

        membroTime = new MembroTime();
        membroTime.setIdMembroTime(1);
        membroTime.setContrato(contrato);
        membroTime.setUsuario(responsavel);
        membroTime.setPapel(PapelTime.MEMBRO);
        membroTime.setAtivo(true);
    }

    @Test
    void criarTarefa_ComDadosValidados_DeveCriarComSucesso() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        when(membroTimeRepository.existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(anyInt(), anyInt()))
            .thenReturn(true);
        when(colunaKanbanRepository.findByContrato_IdContratoAndTipoAndOrdem(anyInt(), anyString(), anyInt()))
            .thenReturn(Optional.of(colunaInicial));
        when(contratoRepository.findById(anyInt())).thenReturn(Optional.of(contrato));
        
        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setTitulo("Validar documento fiscal");
        tarefa.setDescricao("Validar documento fiscal do cliente");
        tarefa.setPrioridade(PrioridadeTarefa.MEDIA);
        tarefa.setResponsavelPrincipal(responsavel);
        tarefa.setDataLimite(LocalDateTime.now().plusDays(7));
        tarefa.setCoResponsaveis(new HashSet<>());
        tarefa.setObservadores(new HashSet<>());
        tarefa.setDependencias(new HashSet<>());

        when(tarefaContratoRepository.save(any(TarefaContrato.class))).thenAnswer(invocation -> {
            TarefaContrato t = invocation.getArgument(0);
            t.setIdTarefa(1);
            return t;
        });

        // Act
        TarefaContrato resultado = tarefaContratoService.criarTarefa(contrato.getIdContrato(), tarefa);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdTarefa());
        assertEquals("Validar documento fiscal", resultado.getTitulo());
        assertEquals(colunaInicial, resultado.getColuna());
        assertEquals(TipoColuna.INICIAL, resultado.getColuna().getTipo());
        
        verify(usuarioLogadoService).exigirFuncionarioAtivo();
        verify(membroTimeRepository).existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(1, 2);
        verify(colunaKanbanRepository).findByContrato_IdContratoAndTipoAndOrdem(1, "INICIAL", 1);
        verify(contratoRepository).findById(1);
        verify(tarefaContratoRepository).save(any(TarefaContrato.class));
    }

    @Test
    void criarTarefa_ComResponsavelNaoMembroTime_DeveLancarExcecao() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        when(membroTimeRepository.existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(anyInt(), anyInt()))
            .thenReturn(false);

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setTitulo("Validar documento fiscal");
        tarefa.setResponsavelPrincipal(responsavel);
        tarefa.setDataLimite(LocalDateTime.now().plusDays(7));

        // Act & Assert
        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            tarefaContratoService.criarTarefa(contrato.getIdContrato(), tarefa);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertTrue(excecao.getReason().contains("Responsável principal deve ser membro ativo do time do contrato"));
        
        verify(usuarioLogadoService).exigirFuncionarioAtivo();
        verify(membroTimeRepository).existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(1, 2);
        verify(tarefaContratoRepository, never()).save(any(TarefaContrato.class));
    }

    @Test
    void moverTarefaParaColunaFinal_ComDependenciasNaoConcluidas_DeveLancarExcecao() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        
        TarefaContrato tarefaExistente = new TarefaContrato();
        tarefaExistente.setIdTarefa(1);
        tarefaExistente.setContrato(contrato);
        tarefaExistente.setColuna(colunaInicial);
        tarefaExistente.setTitulo("Validar documento fiscal");
        tarefaExistente.setResponsavelPrincipal(responsavel);
        tarefaExistente.setDataLimite(LocalDateTime.now().plusDays(7));

        when(tarefaContratoRepository.findById(1)).thenReturn(Optional.of(tarefaExistente));

        ColunaKanban colunaFinal = new ColunaKanban();
        colunaFinal.setIdColuna(2);
        colunaFinal.setContrato(contrato);
        colunaFinal.setNome("Concluída");
        colunaFinal.setTipo(TipoColuna.FINAL);

        when(colunaKanbanRepository.findById(2)).thenReturn(Optional.of(colunaFinal));

        TarefaDependencia dependencia = new TarefaDependencia();
        TarefaContrato tarefaDependente = new TarefaContrato();
        tarefaDependente.setIdTarefa(3);
        tarefaDependente.setColuna(colunaInicial); // Ainda não concluída
        tarefaDependente.setResponsavelPrincipal(responsavel);
        dependencia.setDependeDe(tarefaDependente);

        when(tarefaDependenciaRepository.findByTarefa_IdTarefa(1)).thenReturn(List.of(dependencia));

        TarefaContrato tarefaAtualizada = new TarefaContrato();
        tarefaAtualizada.setColuna(colunaFinal);
        tarefaAtualizada.setResponsavelPrincipal(responsavel);

        // Act & Assert
        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            tarefaContratoService.atualizarTarefa(1, tarefaAtualizada);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertTrue(excecao.getReason().contains("Não é possível mover tarefa para coluna FINAL enquanto suas dependências não estiverem concluídas"));
        
        verify(tarefaDependenciaRepository).findByTarefa_IdTarefa(1);
        verify(tarefaContratoRepository, never()).save(any(TarefaContrato.class));
    }

    @Test
    void adicionarDependencia_ComAutoDependencia_DeveLancarExcecao() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        
        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(1);
        tarefa.setContrato(contrato);

        when(tarefaContratoRepository.findById(1)).thenReturn(Optional.of(tarefa));
        when(tarefaContratoRepository.findById(1)).thenReturn(Optional.of(tarefa));

        // Act & Assert
        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            tarefaContratoService.adicionarDependencia(1, 1);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertTrue(excecao.getReason().contains("Tarefa não pode depender de si mesma"));
        
        verify(tarefaDependenciaRepository, never()).save(any(TarefaDependencia.class));
    }

    @Test
    void adicionarDependencia_ComTarefasDeContratosDiferentes_DeveLancarExcecao() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        
        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(1);
        tarefa.setContrato(contrato);

        Contrato contrato2 = new Contrato();
        contrato2.setIdContrato(2);

        TarefaContrato dependeDe = new TarefaContrato();
        dependeDe.setIdTarefa(2);
        dependeDe.setContrato(contrato2);

        when(tarefaContratoRepository.findById(1)).thenReturn(Optional.of(tarefa));
        when(tarefaContratoRepository.findById(2)).thenReturn(Optional.of(dependeDe));

        // Act & Assert
        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            tarefaContratoService.adicionarDependencia(1, 2);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertTrue(excecao.getReason().contains("Tarefas de dependência devem pertencer ao mesmo contrato"));
        
        verify(tarefaDependenciaRepository, never()).save(any(TarefaDependencia.class));
    }

    @Test
    void excluirTarefa_ComDependentes_DeveLancarExcecao() {
        // Arrange
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);
        
        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(1);
        tarefa.setContrato(contrato);

        when(tarefaContratoRepository.findById(1)).thenReturn(Optional.of(tarefa));

        TarefaDependencia dependencia = new TarefaDependencia();
        when(tarefaDependenciaRepository.findByDependeDe_IdTarefa(1)).thenReturn(List.of(dependencia));

        // Act & Assert
        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            tarefaContratoService.excluirTarefa(1);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertTrue(excecao.getReason().contains("Não é possível excluir tarefa que possui dependências de outras tarefas"));
        
        verify(tarefaContratoRepository, never()).delete(any(TarefaContrato.class));
    }

    @Test
    void moverTarefa_ColunaDestinoDeOutroContrato_DeveLancar400() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(10);
        tarefa.setContrato(contrato);
        tarefa.setColuna(colunaInicial);
        when(tarefaContratoRepository.findById(10)).thenReturn(Optional.of(tarefa));

        Contrato outroContrato = new Contrato();
        outroContrato.setIdContrato(99);
        ColunaKanban colunaOutro = new ColunaKanban();
        colunaOutro.setIdColuna(5);
        colunaOutro.setContrato(outroContrato);
        colunaOutro.setTipo(TipoColuna.INTERMEDIARIA);
        when(colunaKanbanRepository.findById(5)).thenReturn(Optional.of(colunaOutro));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tarefaContratoService.moverTarefa(10, 5));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("não pertence ao contrato"));
        verify(tarefaContratoRepository, never()).save(any());
    }

    @Test
    void moverTarefa_ParaColunaNormal_DeveSalvarEauditar() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(10);
        tarefa.setTitulo("X");
        tarefa.setContrato(contrato);
        tarefa.setColuna(colunaInicial);
        when(tarefaContratoRepository.findById(10)).thenReturn(Optional.of(tarefa));

        ColunaKanban destino = new ColunaKanban();
        destino.setIdColuna(2);
        destino.setContrato(contrato);
        destino.setTipo(TipoColuna.INTERMEDIARIA);
        when(colunaKanbanRepository.findById(2)).thenReturn(Optional.of(destino));
        when(tarefaContratoRepository.save(any(TarefaContrato.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaContrato movida = tarefaContratoService.moverTarefa(10, 2);

        assertEquals(destino, movida.getColuna());
        assertNull(movida.getDataConclusao());
        verify(tarefaContratoRepository).save(tarefa);
        verify(auditoriaService).registrarEvento(
                eq(com.climbe.api_climbe.model.enums.TipoEventoAuditoria.TAREFA_MOVIDA),
                eq("TAREFA"), eq(10), any());
    }

    @Test
    void moverTarefa_ParaColunaFinalComDependenciasPendentes_DeveLancar400() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(10);
        tarefa.setContrato(contrato);
        tarefa.setColuna(colunaInicial);
        when(tarefaContratoRepository.findById(10)).thenReturn(Optional.of(tarefa));

        ColunaKanban colunaFinal = new ColunaKanban();
        colunaFinal.setIdColuna(9);
        colunaFinal.setContrato(contrato);
        colunaFinal.setTipo(TipoColuna.FINAL);
        when(colunaKanbanRepository.findById(9)).thenReturn(Optional.of(colunaFinal));

        // Dependência ainda em coluna não-FINAL
        TarefaContrato dependeDe = new TarefaContrato();
        dependeDe.setIdTarefa(5);
        dependeDe.setColuna(colunaInicial);
        TarefaDependencia dep = new TarefaDependencia();
        dep.setTarefa(tarefa);
        dep.setDependeDe(dependeDe);
        when(tarefaDependenciaRepository.findByTarefa_IdTarefa(10)).thenReturn(List.of(dep));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tarefaContratoService.moverTarefa(10, 9));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(tarefaContratoRepository, never()).save(any());
    }

    @Test
    void moverTarefa_MesmaColuna_DeveSerNoOp() {
        when(usuarioLogadoService.exigirFuncionarioAtivo()).thenReturn(usuarioLogado);

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setIdTarefa(10);
        tarefa.setContrato(contrato);
        tarefa.setColuna(colunaInicial);
        when(tarefaContratoRepository.findById(10)).thenReturn(Optional.of(tarefa));
        when(colunaKanbanRepository.findById(colunaInicial.getIdColuna())).thenReturn(Optional.of(colunaInicial));

        TarefaContrato resultado = tarefaContratoService.moverTarefa(10, colunaInicial.getIdColuna());

        assertSame(tarefa, resultado);
        verify(tarefaContratoRepository, never()).save(any());
        verify(auditoriaService, never()).registrarEvento(any(), any(), any(), any());
    }
}
