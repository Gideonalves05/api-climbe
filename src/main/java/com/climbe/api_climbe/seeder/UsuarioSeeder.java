package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.model.Permissao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.repository.CargoRepository;
import com.climbe.api_climbe.repository.PermissaoRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class UsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PermissaoRepository permissaoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando UsuarioSeeder - garantindo usuários de teste para desenvolvimento");

        // Buscar ou criar cargo ADMIN
        Cargo cargoAdmin = cargoRepository.findByNomeCargo("ADMIN")
            .orElseGet(() -> {
                log.warn("Cargo ADMIN não encontrado, criando...");
                Cargo cargo = new Cargo();
                cargo.setNomeCargo("ADMIN");
                return cargoRepository.save(cargo);
            });

        // Buscar ou criar cargo ANALISTA
        Cargo cargoAnalista = cargoRepository.findByNomeCargo("ANALISTA")
            .orElseGet(() -> {
                log.warn("Cargo ANALISTA não encontrado, criando...");
                Cargo cargo = new Cargo();
                cargo.setNomeCargo("ANALISTA");
                return cargoRepository.save(cargo);
            });

        // Criar ou sincronizar usuário ADMIN com todas as permissões do catálogo
        Set<Permissao> todasPermissoes = new HashSet<>(permissaoRepository.findAll());
        Usuario admin = usuarioRepository.findByEmailIgnoreCase("admin@climbe.com.br")
                .orElseGet(() -> {
                    Usuario novo = new Usuario();
                    novo.setNomeCompleto("Admin Climbe");
                    novo.setEmail("admin@climbe.com.br");
                    novo.setCpf("00000000000");
                    novo.setContato("(11) 99999-9999");
                    novo.setSenhaHash(passwordEncoder.encode("climbe2026"));
                    novo.setSituacao(SituacaoUsuario.ATIVO);
                    novo.setCargo(cargoAdmin);
                    log.info("Usuário ADMIN criado: admin@climbe.com.br / climbe2026");
                    return novo;
                });
        // Sempre garante que o admin possua TODAS as permissões atuais do catálogo
        // (necessário quando novas permissões são adicionadas ao enum CodigoPermissao).
        int antes = admin.getPermissoes() != null ? admin.getPermissoes().size() : 0;
        admin.setPermissoes(todasPermissoes);
        usuarioRepository.save(admin);
        if (antes != todasPermissoes.size()) {
            log.info("Permissões do ADMIN sincronizadas: {} -> {} (catálogo completo)",
                    antes, todasPermissoes.size());
        }

        // Buscar permissões necessárias para Kanban e Tarefas
        Set<Permissao> permissoesAnalista = new HashSet<>();
        List<String> codigosPermissoes = List.of(
            "TAREFA_VER",
            "TAREFA_CRIAR",
            "TAREFA_EDITAR",
            "TAREFA_EXCLUIR",
            "KANBAN_GERENCIAR_COLUNAS",
            "TIME_CONTRATO_VER",
            "TIME_CONTRATO_GERENCIAR"
        );
        
        for (String codigo : codigosPermissoes) {
            permissaoRepository.findByCodigo(codigo).ifPresent(permissoesAnalista::add);
        }

        // Criar usuário de teste ANALISTA se não existir
        if (usuarioRepository.findByEmailIgnoreCase("analista@climbe.com.br").isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNomeCompleto("Ana Souza");
            usuario.setEmail("analista@climbe.com.br");
            usuario.setCpf("12345678901");
            usuario.setContato("(11) 99999-9999");
            usuario.setSenhaHash(passwordEncoder.encode("Senha@123"));
            usuario.setSituacao(SituacaoUsuario.ATIVO);
            usuario.setCargo(cargoAnalista);
            usuario.setPermissoes(permissoesAnalista);

            usuarioRepository.save(usuario);
            log.info("Usuário de teste criado: analista@climbe.com.br / Senha@123");
        } else {
            log.info("Usuário de teste já existe: analista@climbe.com.br");
        }

        log.info("UsuarioSeeder concluído com sucesso");
    }
}
