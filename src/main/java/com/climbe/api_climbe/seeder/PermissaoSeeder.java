package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.model.Permissao;
import com.climbe.api_climbe.repository.PermissaoRepository;
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
@Order(1)
public class PermissaoSeeder implements ApplicationRunner {

    private final PermissaoRepository permissaoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Iniciando PermissaoSeeder - garantindo catálogo de permissões");

        for (CodigoPermissao codigo : CodigoPermissao.values()) {
            Optional<Permissao> permissaoExistente = permissaoRepository.findByCodigo(codigo.name());

            if (permissaoExistente.isEmpty()) {
                // Criar nova permissão
                Permissao novaPermissao = new Permissao();
                novaPermissao.setCodigo(codigo.name());
                novaPermissao.setDescricao(codigo.getDescricaoPadrao());
                permissaoRepository.save(novaPermissao);
                log.info("Criada permissão: {} - {}", codigo.name(), codigo.getDescricaoPadrao());
            } else {
                // Atualizar descrição se estiver vazia
                Permissao permissao = permissaoExistente.get();
                if (permissao.getDescricao() == null || permissao.getDescricao().isBlank()) {
                    permissao.setDescricao(codigo.getDescricaoPadrao());
                    permissaoRepository.save(permissao);
                    log.info("Atualizada descrição da permissão: {}", codigo.name());
                }
            }
        }

        alertarCodigosOrfaos();
        log.info("PermissaoSeeder concluído com sucesso");
    }

    private void alertarCodigosOrfaos() {
        // Verificar se há códigos no banco que não estão no enum
        permissaoRepository.findAll().forEach(permissao -> {
            if (permissao.getCodigo() != null) {
                try {
                    CodigoPermissao.valueOf(permissao.getCodigo());
                } catch (IllegalArgumentException e) {
                    log.warn("Código de permissão órfão no banco: {} (ID: {}) - não está no enum CodigoPermissao",
                            permissao.getCodigo(), permissao.getIdPermissao());
                }
            }
        });
    }
}
