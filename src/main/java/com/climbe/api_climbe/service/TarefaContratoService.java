package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.CriarTarefaDto;
import com.climbe.api_climbe.dto.CriarTarefaLinkDto;
import com.climbe.api_climbe.model.*;
import com.climbe.api_climbe.model.enums.PapelTime;
import com.climbe.api_climbe.model.enums.PrioridadeTarefa;
import com.climbe.api_climbe.model.enums.TipoColuna;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TarefaContratoService {

    private final TarefaContratoRepository tarefaContratoRepository;
    private final ColunaKanbanRepository colunaKanbanRepository;
    private final MembroTimeRepository membroTimeRepository;
    private final TarefaDependenciaRepository tarefaDependenciaRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final ContratoRepository contratoRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final TarefaChecklistItemRepository tarefaChecklistItemRepository;
    private final TarefaLinkRepository tarefaLinkRepository;

    /**
     * Cria uma tarefa a partir do DTO recebido pelo controller, preenchendo todos os campos
     * (responsáveis, co-responsáveis, observadores, subtarefas, links). Os usuários selecionados
     * como responsáveis que ainda não fazem parte do time do contrato são adicionados
     * automaticamente como membros (papel MEMBRO).
     */
    public TarefaContrato criarTarefaFromDto(Integer contratoId, CriarTarefaDto dto) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado"));

        // Primeira coluna INICIAL (mesma convenção da seed)
        ColunaKanban primeiraInicial = colunaKanbanRepository
                .findByContrato_IdContratoAndTipoAndOrdem(contratoId, "INICIAL", 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Contrato não possui coluna INICIAL configurada"));

        Integer respId = dto.responsavelPrincipalId() != null
                ? dto.responsavelPrincipalId()
                : usuarioLogado.getIdUsuario();
        Usuario principal = usuarioRepository.findById(respId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Responsável principal não encontrado"));

        TarefaContrato tarefa = new TarefaContrato();
        tarefa.setContrato(contrato);
        tarefa.setColuna(primeiraInicial);
        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setPrioridade(dto.prioridade() != null ? dto.prioridade() : PrioridadeTarefa.MEDIA);
        tarefa.setResponsavelPrincipal(principal);
        tarefa.setDataInicio(dto.dataInicio());
        tarefa.setDataLimite(dto.dataLimite());
        tarefa.setCriadoPor(usuarioLogado);

        // Co-responsáveis
        if (dto.coResponsaveisIds() != null) {
            Set<Usuario> coResp = new HashSet<>();
            for (Integer uid : dto.coResponsaveisIds()) {
                if (uid == null || uid.equals(principal.getIdUsuario())) continue;
                usuarioRepository.findById(uid).ifPresent(coResp::add);
            }
            tarefa.setCoResponsaveis(coResp);
        }

        // Observadores
        if (dto.observadoresIds() != null) {
            Set<Usuario> obs = new HashSet<>();
            for (Integer uid : dto.observadoresIds()) {
                if (uid == null) continue;
                usuarioRepository.findById(uid).ifPresent(obs::add);
            }
            tarefa.setObservadores(obs);
        }

        TarefaContrato salvo = tarefaContratoRepository.save(tarefa);

        // Auto-adiciona responsáveis (principal + co) ao time do contrato se ainda não estiverem
        List<Usuario> todosResponsaveis = new ArrayList<>();
        todosResponsaveis.add(principal);
        todosResponsaveis.addAll(tarefa.getCoResponsaveis());
        for (Usuario u : todosResponsaveis) {
            garantirMembroTime(contrato, u);
        }

        // Subtarefas (checklist)
        if (dto.subtarefas() != null) {
            int ordem = 1;
            for (String descricao : dto.subtarefas()) {
                if (descricao == null || descricao.isBlank()) continue;
                TarefaChecklistItem item = new TarefaChecklistItem();
                item.setTarefa(salvo);
                item.setDescricao(descricao.trim());
                item.setConcluido(false);
                item.setOrdem(ordem++);
                tarefaChecklistItemRepository.save(item);
            }
        }

        // Links
        if (dto.links() != null) {
            for (CriarTarefaLinkDto link : dto.links()) {
                if (link == null || link.url() == null || link.url().isBlank()) continue;
                TarefaLink l = new TarefaLink();
                l.setTarefa(salvo);
                l.setUrl(link.url().trim());
                l.setTitulo(link.titulo());
                tarefaLinkRepository.save(l);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("contratoId", contratoId);
        payload.put("titulo", salvo.getTitulo());
        payload.put("responsavelId", principal.getIdUsuario());
        payload.put("colunaId", salvo.getColuna().getIdColuna());
        payload.put("dataLimite", salvo.getDataLimite());
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.TAREFA_CRIADA,
                "TAREFA",
                salvo.getIdTarefa(),
                payload
        );

        return salvo;
    }

    private void garantirMembroTime(Contrato contrato, Usuario usuario) {
        boolean jaMembro = membroTimeRepository
                .existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(
                        contrato.getIdContrato(), usuario.getIdUsuario());
        if (jaMembro) return;
        MembroTime mt = new MembroTime();
        mt.setContrato(contrato);
        mt.setUsuario(usuario);
        mt.setPapel(PapelTime.MEMBRO);
        mt.setDataEntrada(LocalDate.now());
        mt.setAtivo(true);
        membroTimeRepository.save(mt);
    }

    /**
     * Recalcula o status da tarefa com base nas subtarefas: se há pelo menos um item de checklist
     * e todos estão concluídos, move a tarefa para a primeira coluna do tipo FINAL do contrato.
     */
    public void atualizarProgressoSubtarefas(Integer tarefaId) {
        TarefaContrato tarefa = tarefaContratoRepository.findById(tarefaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
        List<TarefaChecklistItem> itens = tarefaChecklistItemRepository
                .findByTarefa_IdTarefaOrderByOrdemAsc(tarefaId);
        if (itens.isEmpty()) return;
        boolean todosConcluidos = itens.stream().allMatch(i -> Boolean.TRUE.equals(i.getConcluido()));
        if (!todosConcluidos) return;
        if (tarefa.getColuna().getTipo() == TipoColuna.FINAL) return;

        ColunaKanban colunaFinal = colunaKanbanRepository
                .findByContrato_IdContratoAndTipoOrderByOrdemAsc(tarefa.getContrato().getIdContrato(), "FINAL")
                .stream().findFirst()
                .orElse(null);
        if (colunaFinal == null) return;
        tarefa.setColuna(colunaFinal);
        tarefa.setDataConclusao(LocalDateTime.now());
        tarefaContratoRepository.save(tarefa);
    }

    public TarefaContrato criarTarefa(Integer contratoId, TarefaContrato tarefa) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        // Validar que responsável principal é membro ativo do time
        validarResponsavelMembroTime(contratoId, tarefa.getResponsavelPrincipal().getIdUsuario());
        
        // Buscar contrato do banco
        Contrato contrato = contratoRepository.findById(contratoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Contrato não encontrado"));
        
        // Criar tarefa na primeira coluna INICIAL
        ColunaKanban primeiraColunaInicial = colunaKanbanRepository
            .findByContrato_IdContratoAndTipoAndOrdem(contratoId, "INICIAL", 1)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Contrato não possui coluna INICIAL configurada"));
        
        tarefa.setColuna(primeiraColunaInicial);
        tarefa.setCriadoPor(usuarioLogado);
        tarefa.setContrato(contrato);
        
        // Validar data limite obrigatória
        if (tarefa.getDataLimite() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Data limite é obrigatória");
        }
        
        // Validar ciclo de dependências se houver
        if (tarefa.getDependencias() != null && !tarefa.getDependencias().isEmpty()) {
            validarCicloDependencias(tarefa);
        }
        
        TarefaContrato salvo = tarefaContratoRepository.save(tarefa);

        Map<String, Object> payload = new HashMap<>();
        payload.put("contratoId", contratoId);
        payload.put("titulo", tarefa.getTitulo());
        payload.put("responsavelId", tarefa.getResponsavelPrincipal().getIdUsuario());
        payload.put("colunaId", tarefa.getColuna().getIdColuna());
        payload.put("dataLimite", tarefa.getDataLimite());

        auditoriaService.registrarEvento(
                TipoEventoAuditoria.TAREFA_CRIADA,
                "TAREFA",
                salvo.getIdTarefa(),
                payload
        );

        return salvo;
    }

    public TarefaContrato atualizarTarefa(Integer id, TarefaContrato tarefaAtualizada) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        TarefaContrato tarefaExistente = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Tarefa não encontrada"));
        
        // Validar que responsável principal é membro ativo do time (se mudou)
        if (!tarefaExistente.getResponsavelPrincipal().getIdUsuario().equals(tarefaAtualizada.getResponsavelPrincipal().getIdUsuario())) {
            validarResponsavelMembroTime(tarefaExistente.getContrato().getIdContrato(), 
                tarefaAtualizada.getResponsavelPrincipal().getIdUsuario());
        }
        
        // Validar mudança de coluna
        if (tarefaAtualizada.getColuna() != null && 
            !tarefaExistente.getColuna().getIdColuna().equals(tarefaAtualizada.getColuna().getIdColuna())) {
            
            ColunaKanban novaColuna = colunaKanbanRepository.findById(tarefaAtualizada.getColuna().getIdColuna())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Coluna não encontrada"));
            
            // Se mudando para coluna FINAL, validar dependências
            if (novaColuna.getTipo() == TipoColuna.FINAL) {
                validarDependenciasConcluidas(id);
                tarefaAtualizada.setDataConclusao(LocalDateTime.now());
            } else if (tarefaExistente.getColuna().getTipo() == TipoColuna.FINAL) {
                // Saindo de coluna FINAL, limpar data de conclusão
                tarefaAtualizada.setDataConclusao(null);
            }
            
            tarefaAtualizada.setColuna(novaColuna);
        }
        
        // Validar ciclo de dependências se houver mudanças
        if (tarefaAtualizada.getDependencias() != null) {
            validarCicloDependencias(tarefaAtualizada);
        }
        
        // Atualizar campos permitidos
        tarefaExistente.setTitulo(tarefaAtualizada.getTitulo());
        tarefaExistente.setDescricao(tarefaAtualizada.getDescricao());
        tarefaExistente.setPrioridade(tarefaAtualizada.getPrioridade());
        tarefaExistente.setDataLimite(tarefaAtualizada.getDataLimite());
        if (tarefaAtualizada.getColuna() != null) {
            tarefaExistente.setColuna(tarefaAtualizada.getColuna());
        }
        if (tarefaAtualizada.getResponsavelPrincipal() != null) {
            tarefaExistente.setResponsavelPrincipal(tarefaAtualizada.getResponsavelPrincipal());
        }
        if (tarefaAtualizada.getCoResponsaveis() != null) {
            tarefaExistente.setCoResponsaveis(tarefaAtualizada.getCoResponsaveis());
        }
        if (tarefaAtualizada.getObservadores() != null) {
            tarefaExistente.setObservadores(tarefaAtualizada.getObservadores());
        }
        if (tarefaAtualizada.getDataConclusao() != null) {
            tarefaExistente.setDataConclusao(tarefaAtualizada.getDataConclusao());
        }
        
        TarefaContrato salvo = tarefaContratoRepository.save(tarefaExistente);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tarefaId", id);
        payload.put("titulo", tarefaExistente.getTitulo());
        payload.put("colunaId", tarefaExistente.getColuna().getIdColuna());
        
        // Check if column changed
        if (tarefaAtualizada.getColuna() != null && 
            !tarefaExistente.getColuna().getIdColuna().equals(tarefaAtualizada.getColuna().getIdColuna())) {
            payload.put("colunaAnteriorId", tarefaExistente.getColuna().getIdColuna());
            payload.put("colunaNovaId", tarefaAtualizada.getColuna().getIdColuna());
            
            auditoriaService.registrarEvento(
                    TipoEventoAuditoria.TAREFA_MOVIDA,
                    "TAREFA",
                    id,
                    payload
            );
        } else {
            auditoriaService.registrarEvento(
                    TipoEventoAuditoria.TAREFA_EDITADA,
                    "TAREFA",
                    id,
                    payload
            );
        }

        return salvo;
    }

    /**
     * Move uma tarefa para a coluna destino. Valida:
     *   - Tarefa e coluna destino pertencem ao mesmo contrato;
     *   - Se destino é FINAL, todas as dependências da tarefa estão concluídas;
     *   - Quando entra em FINAL registra data de conclusão; quando sai, limpa.
     */
    public TarefaContrato moverTarefa(Integer tarefaId, Integer colunaDestinoId) {
        usuarioLogadoService.exigirFuncionarioAtivo();

        TarefaContrato tarefa = tarefaContratoRepository.findById(tarefaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));

        ColunaKanban destino = colunaKanbanRepository.findById(colunaDestinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coluna destino não encontrada"));

        if (!destino.getContrato().getIdContrato().equals(tarefa.getContrato().getIdContrato())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Coluna destino não pertence ao contrato da tarefa");
        }

        Integer colunaAnteriorId = tarefa.getColuna().getIdColuna();
        if (colunaAnteriorId.equals(colunaDestinoId)) {
            return tarefa; // no-op
        }

        TipoColuna tipoAnterior = tarefa.getColuna().getTipo();
        if (destino.getTipo() == TipoColuna.FINAL) {
            validarDependenciasConcluidas(tarefaId);
            tarefa.setDataConclusao(LocalDateTime.now());
        } else if (tipoAnterior == TipoColuna.FINAL) {
            tarefa.setDataConclusao(null);
        }

        tarefa.setColuna(destino);
        TarefaContrato salvo = tarefaContratoRepository.save(tarefa);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tarefaId", tarefaId);
        payload.put("titulo", tarefa.getTitulo());
        payload.put("colunaAnteriorId", colunaAnteriorId);
        payload.put("colunaNovaId", colunaDestinoId);
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.TAREFA_MOVIDA,
                "TAREFA",
                tarefaId,
                payload
        );

        return salvo;
    }

    public void excluirTarefa(Integer id) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        TarefaContrato tarefa = tarefaContratoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Tarefa não encontrada"));
        
        // Verificar se há tarefas que dependem desta
        List<TarefaDependencia> dependentes = tarefaDependenciaRepository.findByDependeDe_IdTarefa(id);
        if (!dependentes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Não é possível excluir tarefa que possui dependências de outras tarefas");
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("tarefaId", id);
        payload.put("titulo", tarefa.getTitulo());
        payload.put("contratoId", tarefa.getContrato().getIdContrato());

        auditoriaService.registrarEvento(
                TipoEventoAuditoria.TAREFA_EXCLUIDA,
                "TAREFA",
                id,
                payload
        );
        
        tarefaContratoRepository.delete(tarefa);
    }

    public TarefaDependencia adicionarDependencia(Integer tarefaId, Integer dependeDeId) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        TarefaContrato tarefa = tarefaContratoRepository.findById(tarefaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Tarefa não encontrada"));
        
        TarefaContrato dependeDe = tarefaContratoRepository.findById(dependeDeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Tarefa de dependência não encontrada"));
        
        // Validar que ambas pertencem ao mesmo contrato
        if (!tarefa.getContrato().getIdContrato().equals(dependeDe.getContrato().getIdContrato())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Tarefas de dependência devem pertencer ao mesmo contrato");
        }
        
        // Validar que não é auto-dependência
        if (tarefaId.equals(dependeDeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Tarefa não pode depender de si mesma");
        }
        
        // Validar unicidade
        if (tarefaDependenciaRepository.existsByTarefa_IdTarefaAndDependeDe_IdTarefa(tarefaId, dependeDeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Dependência já existe");
        }
        
        TarefaDependencia dependencia = new TarefaDependencia();
        dependencia.setTarefa(tarefa);
        dependencia.setDependeDe(dependeDe);
        
        // Validar ciclo antes de salvar
        validarCicloDependencias(tarefa);
        
        return tarefaDependenciaRepository.save(dependencia);
    }

    public void removerDependencia(Integer tarefaId, Integer dependenciaId) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        TarefaDependencia dependencia = tarefaDependenciaRepository.findById(dependenciaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Dependência não encontrada"));
        
        if (!dependencia.getTarefa().getIdTarefa().equals(tarefaId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Dependência não pertence à tarefa especificada");
        }
        
        tarefaDependenciaRepository.delete(dependencia);
    }

    private void validarResponsavelMembroTime(Integer contratoId, Integer usuarioId) {
        if (!membroTimeRepository.existsByContrato_IdContratoAndUsuario_IdUsuarioAndAtivoTrue(contratoId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Responsável principal deve ser membro ativo do time do contrato");
        }
    }

    private void validarDependenciasConcluidas(Integer tarefaId) {
        List<TarefaDependencia> dependencias = tarefaDependenciaRepository.findByTarefa_IdTarefa(tarefaId);
        
        for (TarefaDependencia dep : dependencias) {
            TarefaContrato dependeDe = dep.getDependeDe();
            if (dependeDe.getColuna().getTipo() != TipoColuna.FINAL) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Não é possível mover tarefa para coluna FINAL enquanto suas dependências não estiverem concluídas");
            }
        }
    }

    private void validarCicloDependencias(TarefaContrato tarefa) {
        // Construir grafo de dependências
        Map<Integer, List<Integer>> grafo = new HashMap<>();
        
        // Adicionar arestas das dependências da tarefa
        if (tarefa.getDependencias() != null) {
            for (TarefaDependencia dep : tarefa.getDependencias()) {
                grafo.computeIfAbsent(tarefa.getIdTarefa(), k -> new ArrayList<>())
                    .add(dep.getDependeDe().getIdTarefa());
            }
        }
        
        // Buscar ciclo usando DFS
        Set<Integer> visitados = new HashSet<>();
        Set<Integer> visitando = new HashSet<>();
        
        for (Integer no : grafo.keySet()) {
            if (temCicloDFS(no, grafo, visitados, visitando)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Ciclo de dependências detectado");
            }
        }
    }

    private boolean temCicloDFS(Integer no, Map<Integer, List<Integer>> grafo, 
                                 Set<Integer> visitados, Set<Integer> visitando) {
        if (visitando.contains(no)) {
            return true; // Ciclo encontrado
        }
        if (visitados.contains(no)) {
            return false; // Já visitado
        }
        
        visitando.add(no);
        
        List<Integer> vizinhos = grafo.getOrDefault(no, new ArrayList<>());
        for (Integer vizinho : vizinhos) {
            if (temCicloDFS(vizinho, grafo, visitados, visitando)) {
                return true;
            }
        }
        
        visitando.remove(no);
        visitados.add(no);
        
        return false;
    }
}
