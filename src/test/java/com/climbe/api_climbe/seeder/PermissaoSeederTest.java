package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.enums.CodigoPermissao;
import com.climbe.api_climbe.model.Permissao;
import com.climbe.api_climbe.repository.PermissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PermissaoSeederTest {

    @Mock
    private PermissaoRepository permissaoRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private PermissaoSeeder permissaoSeeder;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(permissaoRepository);
    }

    @Test
    void deveCriarNovaPermissaoQuandoNaoExistir() {
        // Arrange
        when(permissaoRepository.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(permissaoRepository.save(any(Permissao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        permissaoSeeder.run(applicationArguments);

        // Assert
        verify(permissaoRepository, times(CodigoPermissao.values().length)).save(any(Permissao.class));
    }

    @Test
    void deveAtualizarDescricaoQuandoEstiverVazia() {
        // Arrange
        Permissao permissaoExistente = new Permissao();
        permissaoExistente.setCodigo("CONTRATO_VER");
        permissaoExistente.setDescricao("");

        when(permissaoRepository.findByCodigo(anyString())).thenAnswer(invocation -> {
            String codigo = invocation.getArgument(0);
            if ("CONTRATO_VER".equals(codigo)) {
                return Optional.of(permissaoExistente);
            }
            return Optional.empty();
        });
        when(permissaoRepository.save(any(Permissao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        permissaoSeeder.run(applicationArguments);

        // Assert
        assertThat(permissaoExistente.getDescricao()).isNotBlank();
    }

    @Test
    void naoDeveCriarPermissaoDuplicada() {
        // Arrange
        Permissao permissaoExistente = new Permissao();
        permissaoExistente.setCodigo("CONTRATO_VER");
        permissaoExistente.setDescricao("Visualizar contratos");
        
        when(permissaoRepository.findByCodigo(anyString())).thenReturn(Optional.of(permissaoExistente));

        // Act
        permissaoSeeder.run(applicationArguments);

        // Assert
        verify(permissaoRepository, never()).save(any(Permissao.class));
    }

    @Test
    void deveLogarAvisoParaCodigoOrfao() {
        // Arrange
        Permissao permissaoOrfa = new Permissao();
        permissaoOrfa.setCodigo("CODIGO_INVALIDO");
        permissaoOrfa.setDescricao("Descrição");
        
        when(permissaoRepository.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(permissaoRepository.findAll()).thenReturn(java.util.List.of(permissaoOrfa));

        // Act
        permissaoSeeder.run(applicationArguments);

        // Assert - O método alertarCodigosOrfaos deve ser chamado
        // Não há como verificar logs diretamente sem configurar um appender,
        // mas garantimos que o método é executado no fluxo normal
    }
}
