package com.climbe.api_climbe.service;

import com.climbe.api_climbe.model.enums.TipoNotificacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GmailApiClientStubTest {

    private GmailApiClientStub gmailApiClientStub;

    @BeforeEach
    void setUp() {
        gmailApiClientStub = new GmailApiClientStub();
    }

    @Test
    void enviarEmail_deveRetornarTrueSemEnviarEmailReal() {
        // Act
        boolean resultado = gmailApiClientStub.enviarEmail(
                "test@example.com",
                TipoNotificacao.TAREFA_ATRIBUIDA,
                "Teste",
                "<p>Corpo do teste</p>"
        );

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void enviarEmail_deveFuncionarComTodosOsTiposDeNotificacao() {
        // Act & Assert
        assertThat(gmailApiClientStub.enviarEmail("test@example.com", TipoNotificacao.TAREFA_ATRIBUIDA, "Teste", "Corpo")).isTrue();
        assertThat(gmailApiClientStub.enviarEmail("test@example.com", TipoNotificacao.TAREFA_VENCIDA, "Teste", "Corpo")).isTrue();
        assertThat(gmailApiClientStub.enviarEmail("test@example.com", TipoNotificacao.REUNIAO, "Teste", "Corpo")).isTrue();
        assertThat(gmailApiClientStub.enviarEmail("test@example.com", TipoNotificacao.ALTERACAO_CONTRATO, "Teste", "Corpo")).isTrue();
    }
}
