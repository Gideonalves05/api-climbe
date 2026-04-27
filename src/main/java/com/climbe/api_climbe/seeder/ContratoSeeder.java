package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.ColunaKanban;
import com.climbe.api_climbe.model.Contrato;
import com.climbe.api_climbe.model.Empresa;
import com.climbe.api_climbe.model.Proposta;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.StatusContrato;
import com.climbe.api_climbe.model.enums.StatusProposta;
import com.climbe.api_climbe.model.enums.TipoColuna;
import com.climbe.api_climbe.repository.ColunaKanbanRepository;
import com.climbe.api_climbe.repository.ContratoRepository;
import com.climbe.api_climbe.repository.EmpresaRepository;
import com.climbe.api_climbe.repository.PropostaRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ContratoSeeder implements CommandLineRunner {

    private final ContratoRepository contratoRepository;
    private final PropostaRepository propostaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ColunaKanbanRepository colunaKanbanRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando ContratoSeeder - garantindo contrato de teste para desenvolvimento");

        // Verificar se já existe contrato
        if (contratoRepository.count() > 0) {
            log.info("Contrato(s) já existe(m) no banco de dados. Garantindo colunas Kanban padrão.");
            contratoRepository.findAll().forEach(this::garantirColunasPadrao);
            return;
        }

        // Criar empresa de teste se não existir
        Empresa empresa = empresaRepository.findByCnpj("12345678000190")
            .orElseGet(() -> {
                log.info("Criando empresa de teste para ContratoSeeder");
                Empresa novaEmpresa = new Empresa();
                novaEmpresa.setCnpj("12345678000190");
                novaEmpresa.setNomeFantasia("Empresa Teste Ltda");
                novaEmpresa.setRazaoSocial("Empresa Teste Ltda");
                novaEmpresa.setEmail("contato@empresateste.com.br");
                novaEmpresa.setRepresentanteContato("(11) 99999-9999");
                return empresaRepository.save(novaEmpresa);
            });

        // Buscar usuário de teste para ser responsável pela proposta
        Usuario usuarioResponsavel = usuarioRepository.findByEmailIgnoreCase("analista@climbe.com.br")
            .orElseThrow(() -> new RuntimeException("Usuário de teste não encontrado. Execute UsuarioSeeder primeiro."));

        // Criar proposta de teste se não existir
        Proposta proposta = propostaRepository.findFirstByEmpresaOrderByDataCriacaoDesc(empresa)
            .orElseGet(() -> {
                log.info("Criando proposta de teste para ContratoSeeder");
                Proposta novaProposta = new Proposta();
                novaProposta.setEmpresa(empresa);
                novaProposta.setUsuarioResponsavel(usuarioResponsavel);
                novaProposta.setDataCriacao(LocalDate.now());
                novaProposta.setStatus(StatusProposta.ACEITA);
                novaProposta.setDocumentoProposta("Documento de teste para desenvolvimento");
                return propostaRepository.save(novaProposta);
            });

        // Criar contrato de teste
        if (contratoRepository.findByProposta(proposta).isEmpty()) {
            Contrato contrato = new Contrato();
            contrato.setProposta(proposta);
            contrato.setEmpresa(proposta.getEmpresa());
            contrato.setDataInicio(LocalDate.now());
            contrato.setDataFim(LocalDate.now().plusYears(1));
            contrato.setStatus(StatusContrato.VIGENTE);

            Contrato salvo = contratoRepository.save(contrato);
            garantirColunasPadrao(salvo);
            log.info("Contrato de teste criado com sucesso para proposta ID: {}", proposta.getIdProposta());
        } else {
            contratoRepository.findByProposta(proposta).ifPresent(this::garantirColunasPadrao);
            log.info("Contrato já existe para a proposta de teste");
        }

        log.info("ContratoSeeder concluído com sucesso");
    }

    private void garantirColunasPadrao(Contrato contrato) {
        if (!colunaKanbanRepository.findByContrato_IdContratoOrderByOrdemAsc(contrato.getIdContrato()).isEmpty()) {
            return;
        }
        log.info("Seedando colunas Kanban padrão no contrato #{}", contrato.getIdContrato());
        salvarColuna(contrato, "A Fazer", 1, TipoColuna.INICIAL, "#f59e0b");
        salvarColuna(contrato, "Em Andamento", 2, TipoColuna.INTERMEDIARIA, "#3b82f6");
        salvarColuna(contrato, "Em Revisão", 3, TipoColuna.INTERMEDIARIA, "#8b5cf6");
        salvarColuna(contrato, "Concluída", 4, TipoColuna.FINAL, "#10b981");
    }

    private void salvarColuna(Contrato contrato, String nome, int ordem, TipoColuna tipo, String cor) {
        ColunaKanban coluna = new ColunaKanban();
        coluna.setContrato(contrato);
        coluna.setNome(nome);
        coluna.setOrdem(ordem);
        coluna.setTipo(tipo);
        coluna.setCor(cor);
        colunaKanbanRepository.save(coluna);
    }
}
