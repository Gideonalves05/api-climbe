package com.climbe.api_climbe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarApiClientStubTest {

    private GoogleCalendarApiClientStub calendarApiClientStub;

    @BeforeEach
    void setUp() {
        calendarApiClientStub = new GoogleCalendarApiClientStub();
    }

    @Test
    void criarEvento_deveRetornarTrueSemCriarEventoReal() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.plusHours(1);
        String[] participantes = new String[]{"test@example.com"};

        // Act
        boolean resultado = calendarApiClientStub.criarEvento(
                "Reunião de Teste",
                "Descrição da reunião",
                inicio,
                fim,
                participantes
        );

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void atualizarEvento_deveRetornarTrueSemAtualizarEventoReal() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = inicio.plusHours(1);

        // Act
        boolean resultado = calendarApiClientStub.atualizarEvento(
                "event123",
                "Reunião Atualizada",
                "Nova descrição",
                inicio,
                fim
        );

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void cancelarEvento_deveRetornarTrueSemCancelarEventoReal() {
        // Act
        boolean resultado = calendarApiClientStub.cancelarEvento("event123");

        // Assert
        assertThat(resultado).isTrue();
    }
}
