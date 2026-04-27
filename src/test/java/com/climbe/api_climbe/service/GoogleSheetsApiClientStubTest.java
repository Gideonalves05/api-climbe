package com.climbe.api_climbe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GoogleSheetsApiClientStubTest {

    private GoogleSheetsApiClientStub sheetsApiClientStub;

    @BeforeEach
    void setUp() {
        sheetsApiClientStub = new GoogleSheetsApiClientStub();
    }

    @Test
    void criarPlanilha_deveRetornarNullSemCriarPlanilhaReal() {
        // Act
        String resultado = sheetsApiClientStub.criarPlanilha("Planilha de Teste");

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void lerDados_deveRetornarNullSemLerDadosReais() {
        // Act
        Object[][] resultado = sheetsApiClientStub.lerDados("spreadsheet123", "Sheet1!A1:D10");

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void escreverDados_deveRetornarTrueSemEscreverDadosReais() {
        // Arrange
        Object[][] valores = new Object[][]{
                {"A1", "B1", "C1"},
                {"A2", "B2", "C2"}
        };

        // Act
        boolean resultado = sheetsApiClientStub.escreverDados("spreadsheet123", "Sheet1!A1:C2", valores);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void adicionarLinha_deveRetornarTrueSemAdicionarLinhaReal() {
        // Arrange
        Object[] valores = new Object[]{"A1", "B1", "C1"};

        // Act
        boolean resultado = sheetsApiClientStub.adicionarLinha("spreadsheet123", "Sheet1", valores);

        // Assert
        assertThat(resultado).isTrue();
    }
}
