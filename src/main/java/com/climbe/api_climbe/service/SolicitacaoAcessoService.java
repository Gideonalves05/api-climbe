package com.climbe.api_climbe.service;

import com.climbe.api_climbe.dto.AtivarContaDto;
import com.climbe.api_climbe.dto.RejeitarUsuarioDto;
import com.climbe.api_climbe.dto.SolicitacaoAcessoDto;
import com.climbe.api_climbe.dto.UsuarioPendenteDto;
import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.model.Permissao;
import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.model.enums.TipoEventoAuditoria;
import com.climbe.api_climbe.model.enums.TipoNotificacao;
import com.climbe.api_climbe.repository.CargoRepository;
import com.climbe.api_climbe.repository.PermissaoRepository;
import com.climbe.api_climbe.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoAcessoService {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PermissaoRepository permissaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final NotificacaoPublisher notificacaoPublisher;

    private static final int TOKEN_EXPIRATION_HOURS = 72;

    @Transactional
    public void solicitarAcesso(SolicitacaoAcessoDto dto) {
        // Check if email already exists
        if (usuarioRepository.findByEmailIgnoreCase(dto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado no sistema");
        }

        // Create new user with PENDENTE_APROVACAO status
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(dto.nomeCompleto());
        usuario.setEmail(dto.email());
        usuario.setSituacao(SituacaoUsuario.PENDENTE_APROVACAO);
        
        // Generate activation token
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);
        usuario.setTokenAtivacao(hashedToken);
        usuario.setTokenExpiraEm(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        
        // Set a temporary password hash (will be replaced on activation)
        usuario.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        
        // Assign default cargo (will be updated by admin)
        Cargo cargoPadrao = cargoRepository.findByNomeCargo("Analista VI Trainee")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cargo padrão não encontrado"));
        usuario.setCargo(cargoPadrao);

        // Store additional info in contato field temporarily (cargoPretendido + motivo)
        String infoAdicional = dto.cargoPretendido() + " | " + dto.motivo();
        usuario.setContato(infoAdicional);

        // Set creation timestamp
        usuario.setCriadoEm(LocalDateTime.now());

        usuarioRepository.save(usuario);

        // Audit event
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.SOLICITACAO_ACESSO_CRIADA,
                "USUARIO",
                usuario.getIdUsuario(),
                Map.of(
                        "email", dto.email(),
                        "nome", dto.nomeCompleto(),
                        "cargoPretendido", dto.cargoPretendido()
                ),
                null
        );

        // Publish notification to admins
        notificarAdminsNovaSolicitacao(usuario, dto);

        log.info("Solicitação de acesso criada para e-mail: {}", dto.email());
    }

    @Transactional(readOnly = true)
    public List<UsuarioPendenteDto> listarPendentes() {
        return usuarioRepository.findBySituacao(SituacaoUsuario.PENDENTE_APROVACAO)
                .stream()
                .map(this::toUsuarioPendenteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void aprovarUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuario.getSituacao() != SituacaoUsuario.PENDENTE_APROVACAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não está pendente de aprovação");
        }

        usuario.setSituacao(SituacaoUsuario.ATIVO);
        usuario.setTokenAtivacao(null);
        usuario.setTokenExpiraEm(null);

        usuarioRepository.save(usuario);

        // Audit event
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.USUARIO_APROVADO,
                "USUARIO",
                idUsuario,
                Map.of("email", usuario.getEmail()),
                null
        );

        log.info("Usuário aprovado: {}", usuario.getEmail());
    }

    @Transactional
    public void rejeitarUsuario(Integer idUsuario, RejeitarUsuarioDto dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (usuario.getSituacao() != SituacaoUsuario.PENDENTE_APROVACAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não está pendente de aprovação");
        }

        usuario.setSituacao(SituacaoUsuario.INATIVO);
        usuario.setMotivoRejeicao(dto.motivo());
        usuario.setTokenAtivacao(null);
        usuario.setTokenExpiraEm(null);

        usuarioRepository.save(usuario);

        // Audit event
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.USUARIO_REJEITADO,
                "USUARIO",
                idUsuario,
                Map.of(
                        "email", usuario.getEmail(),
                        "motivo", dto.motivo()
                ),
                null
        );

        log.info("Usuário rejeitado: {} - Motivo: {}", usuario.getEmail(), dto.motivo());
    }

    @Transactional
    public void ativarConta(AtivarContaDto dto) {
        if (!dto.senha().equals(dto.confirmacaoSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senhas não conferem");
        }

        Usuario usuario = usuarioRepository.findByTokenAtivacao(dto.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido"));

        if (usuario.getSituacao() != SituacaoUsuario.ATIVO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não está aprovado para ativação");
        }

        if (usuario.getTokenExpiraEm() != null && usuario.getTokenExpiraEm().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado");
        }

        // Set new password
        usuario.setSenhaHash(passwordEncoder.encode(dto.senha()));
        usuario.setTokenAtivacao(null);
        usuario.setTokenExpiraEm(null);

        usuarioRepository.save(usuario);

        // Audit event
        auditoriaService.registrarEvento(
                TipoEventoAuditoria.USUARIO_ATIVADO,
                "USUARIO",
                usuario.getIdUsuario(),
                Map.of("email", usuario.getEmail()),
                null
        );

        log.info("Conta ativada para usuário: {}", usuario.getEmail());
    }

    private UsuarioPendenteDto toUsuarioPendenteDto(Usuario usuario) {
        // Parse contato field to extract cargoPretendido and motivo
        String cargoPretendido = "";
        String motivo = "";
        if (usuario.getContato() != null && usuario.getContato().contains(" | ")) {
            String[] parts = usuario.getContato().split(" \\| ", 2);
            if (parts.length >= 2) {
                cargoPretendido = parts[0];
                motivo = parts[1];
            }
        }

        return new UsuarioPendenteDto(
                usuario.getIdUsuario(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                cargoPretendido,
                motivo,
                usuario.getSituacao(),
                usuario.getCriadoEm()
        );
    }

    private String hashToken(String token) {
        // Simple hash using SHA-256 (in production, consider using a more secure method)
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao gerar hash do token");
        }
    }

    private void notificarAdminsNovaSolicitacao(Usuario usuario, SolicitacaoAcessoDto dto) {
        // Find users with USUARIO_APROVAR permission
        Permissao permissaoAprovar = permissaoRepository.findByCodigo("USUARIO_APROVAR")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Permissão USUARIO_APROVAR não encontrada"));

        Set<Usuario> admins = usuarioRepository.findAll().stream()
                .filter(u -> u.getSituacao() == SituacaoUsuario.ATIVO)
                .filter(u -> u.getPermissoes().contains(permissaoAprovar))
                .collect(Collectors.toSet());

        if (!admins.isEmpty()) {
            notificacaoPublisher.publicar(
                    admins,
                    TipoNotificacao.SOLICITACAO_ACESSO_PENDENTE,
                    "Nova solicitação de acesso",
                    String.format("%s (%s) solicitou acesso ao sistema para a função de %s.",
                            dto.nomeCompleto(), dto.email(), dto.cargoPretendido()),
                    "/admin/usuarios/pendentes",
                    Map.of(
                            "usuarioId", usuario.getIdUsuario(),
                            "email", dto.email(),
                            "nome", dto.nomeCompleto(),
                            "cargoPretendido", dto.cargoPretendido(),
                            "motivo", dto.motivo()
                    )
            );
            log.info("Notificação enviada para {} administradores sobre nova solicitação", admins.size());
        }
    }
}
