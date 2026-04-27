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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SolicitacaoAcessoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private PermissaoRepository permissaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private NotificacaoPublisher notificacaoPublisher;

    @InjectMocks
    private SolicitacaoAcessoService solicitacaoAcessoService;

    private Cargo cargoPadrao;
    private Permissao permissaoAprovar;
    private Usuario adminUsuario;

    @BeforeEach
    void setUp() {
        cargoPadrao = new Cargo();
        cargoPadrao.setIdCargo(1);
        cargoPadrao.setNomeCargo("Analista VI Trainee");

        permissaoAprovar = new Permissao();
        permissaoAprovar.setIdPermissao(1);
        permissaoAprovar.setCodigo("USUARIO_APROVAR");
        permissaoAprovar.setDescricao("Aprovar usuários pendentes");

        adminUsuario = new Usuario();
        adminUsuario.setIdUsuario(100);
        adminUsuario.setEmail("admin@climbe.com");
        adminUsuario.setNomeCompleto("Admin User");
        adminUsuario.setSituacao(SituacaoUsuario.ATIVO);
        adminUsuario.setPermissoes(Set.of(permissaoAprovar));

        when(cargoRepository.findByNomeCargo("Analista VI Trainee")).thenReturn(Optional.of(cargoPadrao));
        when(permissaoRepository.findByCodigo("USUARIO_APROVAR")).thenReturn(Optional.of(permissaoAprovar));
        when(usuarioRepository.findAll()).thenReturn(List.of(adminUsuario));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
    }

    @Test
    void solicitarAcesso_deveCriarUsuarioPendenteComToken() {
        // Arrange
        SolicitacaoAcessoDto dto = new SolicitacaoAcessoDto(
                "João Silva",
                "joao.silva@climbe.com",
                "Analista Financeiro",
                "Preciso acessar o sistema para acompanhar contratos"
        );

        when(usuarioRepository.findByEmailIgnoreCase("joao.silva@climbe.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            if (u.getIdUsuario() == null) {
                u.setIdUsuario(1);
            }
            return u;
        });

        // Act
        solicitacaoAcessoService.solicitarAcesso(dto);

        // Assert
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getNomeCompleto()).isEqualTo("João Silva");
        assertThat(usuarioSalvo.getEmail()).isEqualTo("joao.silva@climbe.com");
        assertThat(usuarioSalvo.getSituacao()).isEqualTo(SituacaoUsuario.PENDENTE_APROVACAO);
        assertThat(usuarioSalvo.getTokenAtivacao()).isNotNull();
        assertThat(usuarioSalvo.getTokenExpiraEm()).isNotNull();
        assertThat(usuarioSalvo.getCriadoEm()).isNotNull();
        assertThat(usuarioSalvo.getContato()).contains("Analista Financeiro");

        verify(auditoriaService).registrarEvento(
                eq(TipoEventoAuditoria.SOLICITACAO_ACESSO_CRIADA),
                eq("USUARIO"),
                any(Integer.class),
                any(),
                isNull()
        );

        verify(notificacaoPublisher).publicar(
                eq(Set.of(adminUsuario)),
                eq(TipoNotificacao.SOLICITACAO_ACESSO_PENDENTE),
                eq("Nova solicitação de acesso"),
                anyString(),
                eq("/admin/usuarios/pendentes"),
                any()
        );
    }

    @Test
    void solicitarAcesso_deveLancarExcecaoQuandoEmailJaExiste() {
        // Arrange
        SolicitacaoAcessoDto dto = new SolicitacaoAcessoDto(
                "João Silva",
                "joao.silva@climbe.com",
                "Analista Financeiro",
                "Preciso acessar o sistema"
        );

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("joao.silva@climbe.com");
        when(usuarioRepository.findByEmailIgnoreCase("joao.silva@climbe.com")).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        assertThatThrownBy(() -> solicitacaoAcessoService.solicitarAcesso(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("E-mail já cadastrado");

        verify(usuarioRepository, never()).save(any());
        verify(auditoriaService, never()).registrarEvento(any(), any(), any(), any(), any());
        verify(notificacaoPublisher, never()).publicar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void listarPendentes_deveRetornarUsuariosPendentes() {
        // Arrange
        Usuario usuarioPendente = new Usuario();
        usuarioPendente.setIdUsuario(1);
        usuarioPendente.setNomeCompleto("João Silva");
        usuarioPendente.setEmail("joao.silva@climbe.com");
        usuarioPendente.setSituacao(SituacaoUsuario.PENDENTE_APROVACAO);
        usuarioPendente.setContato("Analista Financeiro | Motivo da solicitação");
        usuarioPendente.setCriadoEm(LocalDateTime.now());

        when(usuarioRepository.findBySituacao(SituacaoUsuario.PENDENTE_APROVACAO))
                .thenReturn(List.of(usuarioPendente));

        // Act
        List<UsuarioPendenteDto> pendentes = solicitacaoAcessoService.listarPendentes();

        // Assert
        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.get(0).idUsuario()).isEqualTo(1);
        assertThat(pendentes.get(0).nomeCompleto()).isEqualTo("João Silva");
        assertThat(pendentes.get(0).email()).isEqualTo("joao.silva@climbe.com");
        assertThat(pendentes.get(0).cargoPretendido()).isEqualTo("Analista Financeiro");
        assertThat(pendentes.get(0).situacao()).isEqualTo(SituacaoUsuario.PENDENTE_APROVACAO);
    }

    @Test
    void aprovarUsuario_deveAtivarUsuarioELimparToken() {
        // Arrange
        Usuario usuarioPendente = new Usuario();
        usuarioPendente.setIdUsuario(1);
        usuarioPendente.setEmail("joao.silva@climbe.com");
        usuarioPendente.setSituacao(SituacaoUsuario.PENDENTE_APROVACAO);
        usuarioPendente.setTokenAtivacao("hashedToken");
        usuarioPendente.setTokenExpiraEm(LocalDateTime.now().plusHours(72));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioPendente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        solicitacaoAcessoService.aprovarUsuario(1);

        // Assert
        assertThat(usuarioPendente.getSituacao()).isEqualTo(SituacaoUsuario.ATIVO);
        assertThat(usuarioPendente.getTokenAtivacao()).isNull();
        assertThat(usuarioPendente.getTokenExpiraEm()).isNull();

        verify(auditoriaService).registrarEvento(
                eq(TipoEventoAuditoria.USUARIO_APROVADO),
                eq("USUARIO"),
                eq(1),
                any(),
                isNull()
        );
    }

    @Test
    void aprovarUsuario_deveLancarExcecaoQuandoUsuarioNaoPendente() {
        // Arrange
        Usuario usuarioAtivo = new Usuario();
        usuarioAtivo.setIdUsuario(1);
        usuarioAtivo.setSituacao(SituacaoUsuario.ATIVO);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioAtivo));

        // Act & Assert
        assertThatThrownBy(() -> solicitacaoAcessoService.aprovarUsuario(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Usuário não está pendente de aprovação");

        verify(usuarioRepository, never()).save(any());
        verify(auditoriaService, never()).registrarEvento(any(), any(), any(), any(), any());
    }

    @Test
    void rejeitarUsuario_deveInativarUsuarioComMotivo() {
        // Arrange
        Usuario usuarioPendente = new Usuario();
        usuarioPendente.setIdUsuario(1);
        usuarioPendente.setEmail("joao.silva@climbe.com");
        usuarioPendente.setSituacao(SituacaoUsuario.PENDENTE_APROVACAO);
        usuarioPendente.setTokenAtivacao("hashedToken");

        RejeitarUsuarioDto dto = new RejeitarUsuarioDto("Cargo não disponível");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioPendente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        solicitacaoAcessoService.rejeitarUsuario(1, dto);

        // Assert
        assertThat(usuarioPendente.getSituacao()).isEqualTo(SituacaoUsuario.INATIVO);
        assertThat(usuarioPendente.getMotivoRejeicao()).isEqualTo("Cargo não disponível");
        assertThat(usuarioPendente.getTokenAtivacao()).isNull();
        assertThat(usuarioPendente.getTokenExpiraEm()).isNull();

        verify(auditoriaService).registrarEvento(
                eq(TipoEventoAuditoria.USUARIO_REJEITADO),
                eq("USUARIO"),
                eq(1),
                any(),
                isNull()
        );
    }

    @Test
    void ativarConta_deveDefinirSenhaEAtivarUsuario() {
        // Arrange
        Usuario usuarioAprovado = new Usuario();
        usuarioAprovado.setIdUsuario(1);
        usuarioAprovado.setEmail("joao.silva@climbe.com");
        usuarioAprovado.setSituacao(SituacaoUsuario.ATIVO);
        usuarioAprovado.setTokenAtivacao("hashedToken");
        usuarioAprovado.setTokenExpiraEm(LocalDateTime.now().plusHours(72));

        AtivarContaDto dto = new AtivarContaDto("rawToken", "NovaSenha@123", "NovaSenha@123");

        when(usuarioRepository.findByTokenAtivacao(anyString())).thenReturn(Optional.of(usuarioAprovado));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        solicitacaoAcessoService.ativarConta(dto);

        // Assert
        assertThat(usuarioAprovado.getTokenAtivacao()).isNull();
        assertThat(usuarioAprovado.getTokenExpiraEm()).isNull();
        verify(passwordEncoder).encode("NovaSenha@123");

        verify(auditoriaService).registrarEvento(
                eq(TipoEventoAuditoria.USUARIO_ATIVADO),
                eq("USUARIO"),
                eq(1),
                any(),
                isNull()
        );
    }

    @Test
    void ativarConta_deveLancarExcecaoQuandoSenhasNaoConferem() {
        // Arrange
        AtivarContaDto dto = new AtivarContaDto("token", "Senha@123", "SenhaDiferente@456");

        // Act & Assert
        assertThatThrownBy(() -> solicitacaoAcessoService.ativarConta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Senhas não conferem");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void ativarConta_deveLancarExcecaoQuandoTokenExpirado() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setSituacao(SituacaoUsuario.ATIVO);
        usuario.setTokenAtivacao("hashedToken");
        usuario.setTokenExpiraEm(LocalDateTime.now().minusHours(1)); // Expirado

        AtivarContaDto dto = new AtivarContaDto("token", "Senha@123", "Senha@123");

        when(usuarioRepository.findByTokenAtivacao(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> solicitacaoAcessoService.ativarConta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Token expirado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void ativarConta_deveLancarExcecaoQuandoUsuarioNaoAprovado() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setSituacao(SituacaoUsuario.PENDENTE_APROVACAO); // Não aprovado
        usuario.setTokenAtivacao("hashedToken");
        usuario.setTokenExpiraEm(LocalDateTime.now().plusHours(72));

        AtivarContaDto dto = new AtivarContaDto("token", "Senha@123", "Senha@123");

        when(usuarioRepository.findByTokenAtivacao(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> solicitacaoAcessoService.ativarConta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Usuário não está aprovado para ativação");

        verify(usuarioRepository, never()).save(any());
    }
}
