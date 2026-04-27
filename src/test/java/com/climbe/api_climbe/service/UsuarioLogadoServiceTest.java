package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.Usuario;
import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.model.enums.SituacaoUsuario;
import com.climbe.api_climbe.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioLogadoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioLogadoService usuarioLogadoService;

    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = new Usuario();
        usuarioAtivo.setIdUsuario(1);
        usuarioAtivo.setEmail("joao@climbe.com");
        usuarioAtivo.setNomeCompleto("João Silva");
        usuarioAtivo.setSituacao(SituacaoUsuario.ATIVO);

        usuarioInativo = new Usuario();
        usuarioInativo.setIdUsuario(2);
        usuarioInativo.setEmail("maria@climbe.com");
        usuarioInativo.setNomeCompleto("Maria Santos");
        usuarioInativo.setSituacao(SituacaoUsuario.INATIVO);
    }

    @Test
    void exigirFuncionarioAtivo_deveRetornarUsuarioQuandoAutenticadoEAtivo() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@climbe.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("joao@climbe.com")).thenReturn(Optional.of(usuarioAtivo));
        SecurityContextHolder.setContext(securityContext);

        // Act
        Usuario result = usuarioLogadoService.exigirFuncionarioAtivo();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("joao@climbe.com");
        assertThat(result.getSituacao()).isEqualTo(SituacaoUsuario.ATIVO);
    }

    @Test
    void exigirFuncionarioAtivo_deveLancar403QuandoUsuarioInativo() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("maria@climbe.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("maria@climbe.com")).thenReturn(Optional.of(usuarioInativo));
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> usuarioLogadoService.exigirFuncionarioAtivo())
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasMessageContaining("Usuário não está ativo");
    }

    @Test
    void exigirFuncionarioAtivo_deveLancar401QuandoNaoAutenticado() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> usuarioLogadoService.exigirFuncionarioAtivo())
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                .hasMessageContaining("Usuário não autenticado");
    }

    @Test
    void exigirFuncionarioAtivo_deveLancar401QuandoAuthenticationNulo() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> usuarioLogadoService.exigirFuncionarioAtivo())
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                .hasMessageContaining("Usuário não autenticado");
    }

    @Test
    void exigirEmpresa_deveLancar403QuandoUsuarioAutenticado() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@climbe.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase("joao@climbe.com")).thenReturn(Optional.of(usuarioAtivo));
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> usuarioLogadoService.exigirEmpresa())
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasMessageContaining("Acesso restrito a empresas");
    }

    @Test
    void exigirEmpresa_naoDeveLancarQuandoNaoAutenticado() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // Act
        usuarioLogadoService.exigirEmpresa();

        // Assert - não deve lançar exceção
    }

    @Test
    void temPermissao_deveRetornarTrueQuandoUsuarioTemPermissao() {
        // Arrange
        Collection<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("CONTRATO_VER"),
                new SimpleGrantedAuthority("TAREFA_CRIAR")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
        SecurityContextHolder.setContext(securityContext);

        // Act
        boolean result = usuarioLogadoService.temPermissao(CodigoPermissao.CONTRATO_VER);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void temPermissao_deveRetornarFalseQuandoUsuarioNaoTemPermissao() {
        // Arrange
        Collection<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("TAREFA_CRIAR")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
        SecurityContextHolder.setContext(securityContext);

        // Act
        boolean result = usuarioLogadoService.temPermissao(CodigoPermissao.CONTRATO_VER);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void temPermissao_deveRetornarFalseQuandoNaoAutenticado() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        // Act
        boolean result = usuarioLogadoService.temPermissao(CodigoPermissao.CONTRATO_VER);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void exigirPermissao_deveRetornarQuandoUsuarioTemPermissao() {
        // Arrange
        Collection<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("CONTRATO_VER")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        usuarioLogadoService.exigirPermissao(CodigoPermissao.CONTRATO_VER);
    }

    @Test
    void exigirPermissao_deveLancar403QuandoUsuarioNaoTemPermissao() {
        // Arrange
        Collection<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("TAREFA_CRIAR")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThatThrownBy(() -> usuarioLogadoService.exigirPermissao(CodigoPermissao.CONTRATO_VER))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN)
                .hasMessageContaining("Permissão necessária");
    }
}
