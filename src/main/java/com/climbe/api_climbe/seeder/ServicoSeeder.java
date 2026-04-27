package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.Servico;
import com.climbe.api_climbe.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class ServicoSeeder implements ApplicationRunner {

    private final ServicoRepository servicoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Iniciando ServicoSeeder - garantindo catálogo de serviços padrão");

        String[] servicosPadrao = {
                "Assessoria Financeira",
                "Captação de Investidores",
                "Consultoria Financeira",
                "Gestão de Investimentos",
                "Planejamento Patrimonial",
                "Assessoria Tributária",
                "Auditoria"
        };

        for (String nomeServico : servicosPadrao) {
            Optional<Servico> servicoExistente = servicoRepository.findByNomeIgnoreCase(nomeServico);

            if (servicoExistente.isEmpty()) {
                Servico novoServico = new Servico();
                novoServico.setNome(nomeServico);
                servicoRepository.save(novoServico);
                log.info("Criado serviço: {}", nomeServico);
            } else {
                log.debug("Serviço já existe: {}", nomeServico);
            }
        }

        log.info("ServicoSeeder concluído com sucesso");
    }
}
