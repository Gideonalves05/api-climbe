package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.ColunaKanban;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.TipoColuna;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.repository.ColunaKanbanRepository;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.TarefaContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ColunaKanbanService {

    private final ColunaKanbanRepository colunaKanbanRepository;
    private final TarefaContratoRepository tarefaContratoRepository;
    private final UsuarioLogadoService usuarioLogadoService;
    private final ContratoRepository contratoRepository;
    private final AuditoriaService auditoriaService;

    public ColunaKanban criarColuna(Integer contratoId, ColunaKanban coluna) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        // Buscar contrato do banco
        Contrato contrato = contratoRepository.findById(contratoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Contrato não encontrado"));
        
        // Validar ordem única
        if (colunaKanbanRepository.existsByContrato_IdContratoAndOrdem(contratoId, coluna.getOrdem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Já existe uma coluna com esta ordem no contrato");
        }
        
        coluna.setContrato(contrato);
        return colunaKanbanRepository.save(coluna);
    }

    public ColunaKanban atualizarColuna(Integer id, ColunaKanban colunaAtualizada) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        ColunaKanban colunaExistente = colunaKanbanRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Coluna não encontrada"));
        
        // Validar ordem única se mudou
        if (!colunaExistente.getOrdem().equals(colunaAtualizada.getOrdem()) &&
            colunaKanbanRepository.existsByContrato_IdContratoAndOrdem(
                colunaExistente.getContrato().getIdContrato(), colunaAtualizada.getOrdem())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Já existe uma coluna com esta ordem no contrato");
        }
        
        colunaExistente.setNome(colunaAtualizada.getNome());
        colunaExistente.setOrdem(colunaAtualizada.getOrdem());
        colunaExistente.setTipo(colunaAtualizada.getTipo());
        colunaExistente.setCor(colunaAtualizada.getCor());
        
        return colunaKanbanRepository.save(colunaExistente);
    }

    public void excluirColuna(Integer id) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        ColunaKanban coluna = colunaKanbanRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Coluna não encontrada"));
        
        // Verificar se há tarefas vinculadas
        if (tarefaContratoRepository.existsTarefasByColunaId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Não é possível excluir coluna que possui tarefas vinculadas");
        }
        
        colunaKanbanRepository.delete(coluna);
    }

    public void reordenarColunas(Integer contratoId, List<ColunaKanban> colunas) {
        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();
        
        // Validar que todas as colunas pertencem ao contrato
        for (ColunaKanban coluna : colunas) {
            ColunaKanban colunaExistente = colunaKanbanRepository.findById(coluna.getIdColuna())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Coluna não encontrada"));
            
            if (!colunaExistente.getContrato().getIdContrato().equals(contratoId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Coluna não pertence ao contrato especificado");
            }
        }
        
        // Atualizar ordens
        for (int i = 0; i < colunas.size(); i++) {
            ColunaKanban coluna = colunas.get(i);
            coluna.setOrdem(i + 1);
            colunaKanbanRepository.save(coluna);
        }
    }

    public void seedColunasPadrao(Integer contratoId) {
        // Criar 4 colunas padrão
        criarColuna(contratoId, new ColunaKanban(null, null, "A Fazer", 1, TipoColuna.INICIAL, "#f59e0b", null, null));
        criarColuna(contratoId, new ColunaKanban(null, null, "Em Andamento", 2, TipoColuna.INTERMEDIARIA, "#3b82f6", null, null));
        criarColuna(contratoId, new ColunaKanban(null, null, "Em Revisão", 3, TipoColuna.INTERMEDIARIA, "#8b5cf6", null, null));
        criarColuna(contratoId, new ColunaKanban(null, null, "Concluída", 4, TipoColuna.FINAL, "#10b981", null, null));
    }

    /**
     * Provisiona as 3 colunas padrão do Kanban para um contrato.
     * Idempotente: se já existirem colunas, retorna as existentes sem criar duplicatas.
     *
     * @param contratoId ID do contrato
     * @param registrarAuditoria se true, registra evento de auditoria KANBAN_INICIALIZADO
     * @return lista de colunas (criadas ou existentes)
     */
    @Transactional
    public List<ColunaKanban> provisionarPadrao(Integer contratoId, boolean registrarAuditoria) {
        Contrato contrato = contratoRepository.findById(contratoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Contrato não encontrado"));

        // Idempotência: verificar se já existem colunas
        long count = colunaKanbanRepository.countByContrato_IdContrato(contratoId);
        if (count > 0) {
            return colunaKanbanRepository.findByContrato_IdContratoOrderByOrdemAsc(contratoId);
        }

        Usuario usuarioLogado = usuarioLogadoService.exigirFuncionarioAtivo();

        List<ColunaKanban> colunasCriadas = new ArrayList<>();

        // Coluna 1: INICIAL (A Fazer)
        ColunaKanban coluna1 = new ColunaKanban();
        coluna1.setContrato(contrato);
        coluna1.setNome("A Fazer");
        coluna1.setOrdem(1);
        coluna1.setTipo(TipoColuna.INICIAL);
        coluna1.setCor("#f59e0b");
        colunasCriadas.add(colunaKanbanRepository.save(coluna1));

        // Coluna 2: INTERMEDIARIA (Em Andamento)
        ColunaKanban coluna2 = new ColunaKanban();
        coluna2.setContrato(contrato);
        coluna2.setNome("Em Andamento");
        coluna2.setOrdem(2);
        coluna2.setTipo(TipoColuna.INTERMEDIARIA);
        coluna2.setCor("#3b82f6");
        colunasCriadas.add(colunaKanbanRepository.save(coluna2));

        // Coluna 3: FINAL (Concluído)
        ColunaKanban coluna3 = new ColunaKanban();
        coluna3.setContrato(contrato);
        coluna3.setNome("Concluído");
        coluna3.setOrdem(3);
        coluna3.setTipo(TipoColuna.FINAL);
        coluna3.setCor("#10b981");
        colunasCriadas.add(colunaKanbanRepository.save(coluna3));

        // Registrar auditoria se solicitado
        if (registrarAuditoria) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("colunasCriadas", 3);
            payload.put("usuarioId", usuarioLogado.getIdUsuario());
            auditoriaService.registrarEvento(
                TipoEventoAuditoria.KANBAN_INICIALIZADO,
                "CONTRATO",
                contratoId,
                payload
            );
        }

        return colunasCriadas;
    }

    /**
     * Variante convenience sem registro de auditoria (para uso interno em criação de contrato).
     */
    @Transactional
    public List<ColunaKanban> provisionarPadrao(Integer contratoId) {
        return provisionarPadrao(contratoId, false);
    }
}
