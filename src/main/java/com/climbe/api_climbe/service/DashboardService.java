package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.DashboardResumoDto;
import com.climbe.api_climbe.model.MembroTime;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.MembroTimeRepository;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ContratoRepository contratoRepository;
    private final TarefaContratoRepository tarefaContratoRepository;
    private final MembroTimeRepository membroTimeRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public DashboardResumoDto obterResumo(String periodo) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();

        boolean temPermissaoGeral = usuarioLogadoService.temPermissao(CodigoPermissao.CONTRATO_VER);
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = calcularInicioPeriodo(periodo, agora);

        // Se não tiver permissão geral, filtrar por contratos do time
        List<Integer> contratoIdsFiltrados = new ArrayList<>();
        if (!temPermissaoGeral) {
            List<MembroTime> membrosTime = membroTimeRepository
                .findByUsuario_IdUsuarioAndAtivoTrue(usuarioLogado.getIdUsuario());
            contratoIdsFiltrados = membrosTime.stream()
                .map(m -> m.getContrato().getIdContrato())
                .collect(Collectors.toList());
            
            if (contratoIdsFiltrados.isEmpty()) {
                return criarResumoVazio();
            }
        }

        DashboardResumoDto.ContratosTotaisDto contratosTotais = buscarTotaisContratos(contratoIdsFiltrados, temPermissaoGeral);
        DashboardResumoDto.TarefasPorStatusDto tarefasPorStatus = buscarTarefasPorStatus();
        Integer tarefasVencendoEm7Dias = contarTarefasVencendoEm7Dias(agora);
        List<DashboardResumoDto.SerieTemporalDto> conclusoesPorDia = buscarConclusoesPorPeriodo(inicioPeriodo, agora);
        List<DashboardResumoDto.ContratoAtrasoDto> topContratosComAtraso = buscarTopContratosComAtraso(agora);

        return new DashboardResumoDto(
            contratosTotais,
            tarefasPorStatus,
            tarefasVencendoEm7Dias,
            conclusoesPorDia,
            topContratosComAtraso
        );
    }

    private LocalDateTime calcularInicioPeriodo(String periodo, LocalDateTime agora) {
        return switch (periodo) {
            case "30d" -> agora.minusDays(30);
            case "90d" -> agora.minusDays(90);
            default -> agora.minusDays(7); // default 7d
        };
    }

    private DashboardResumoDto.ContratosTotaisDto buscarTotaisContratos(List<Integer> contratoIds, boolean temPermissaoGeral) {
        if (!temPermissaoGeral && contratoIds.isEmpty()) {
            return new DashboardResumoDto.ContratosTotaisDto(0L, 0L, 0L, 0L);
        }
        
        // Se tiver permissão geral, busca todos. Se não, precisaria filtrar por contratoIds
        // Por simplicidade, se não tiver permissão geral, retorna zeros (seria melhor ter queries específicas)
        if (!temPermissaoGeral) {
            return new DashboardResumoDto.ContratosTotaisDto(0L, 0L, 0L, 0L);
        }

        long ativos = contratoRepository.countByStatus(StatusContrato.VIGENTE);
        long encerrados = contratoRepository.countByStatus(StatusContrato.ENCERRADO);
        long suspensos = contratoRepository.countByStatus(StatusContrato.SUSPENSO);
        long cancelados = contratoRepository.countByStatus(StatusContrato.CANCELADO);

        return new DashboardResumoDto.ContratosTotaisDto(ativos, encerrados, suspensos, cancelados);
    }

    private DashboardResumoDto.TarefasPorStatusDto buscarTarefasPorStatus() {
        long aFazer = tarefaContratoRepository.countByColunaTipoInicial();
        long emAndamento = tarefaContratoRepository.countByColunaTipoIntermediaria();
        long concluidas = tarefaContratoRepository.countByColunaTipoFinal();
        long bloqueadas = tarefaContratoRepository.countVencidas(LocalDateTime.now());

        return new DashboardResumoDto.TarefasPorStatusDto(aFazer, emAndamento, concluidas, bloqueadas);
    }

    private Integer contarTarefasVencendoEm7Dias(LocalDateTime agora) {
        LocalDateTime daqui7Dias = agora.plusDays(7);
        long count = tarefaContratoRepository.countVencendoNoPeriodo(agora, daqui7Dias);
        return (int) count;
    }

    private List<DashboardResumoDto.SerieTemporalDto> buscarConclusoesPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> resultados = tarefaContratoRepository.findConclusoesPorPeriodo(inicio, fim);
        
        return resultados.stream()
            .map(row -> new DashboardResumoDto.SerieTemporalDto(
                (LocalDate) row[0],
                (Long) row[1]
            ))
            .collect(Collectors.toList());
    }

    private List<DashboardResumoDto.ContratoAtrasoDto> buscarTopContratosComAtraso(LocalDateTime agora) {
        List<Object[]> resultados = tarefaContratoRepository.findContratosComMaisTarefasVencidas(agora);
        
        // Limitar a top 5
        List<Object[]> top5 = resultados.stream()
            .limit(5)
            .collect(Collectors.toList());
        
        if (top5.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> contratoIds = top5.stream()
            .map(row -> (Integer) row[0])
            .collect(Collectors.toList());
        
        List<Object[]> nomesEmpresas = contratoRepository.findNomesEmpresasPorIds(contratoIds);
        Map<Integer, String> mapaNomes = nomesEmpresas.stream()
            .collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> (String) row[1]
            ));

        return top5.stream()
            .map(row -> new DashboardResumoDto.ContratoAtrasoDto(
                (Integer) row[0],
                mapaNomes.getOrDefault(row[0], "Empresa não encontrada"),
                ((Long) row[1]).intValue()
            ))
            .collect(Collectors.toList());
    }

    private DashboardResumoDto criarResumoVazio() {
        return new DashboardResumoDto(
            new DashboardResumoDto.ContratosTotaisDto(0L, 0L, 0L, 0L),
            new DashboardResumoDto.TarefasPorStatusDto(0L, 0L, 0L, 0L),
            0,
            new ArrayList<>(),
            new ArrayList<>()
        );
    }
}
