package com.climbe.api_climbe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GoogleDriveApiClientStubTest {

    private GoogleDriveApiClientStub driveApiClientStub;

    @BeforeEach
    void setUp() {
        driveApiClientStub = new GoogleDriveApiClientStub();
    }

    @Test
    void uploadArquivo_deveRetornarNullSemFazerUploadReal() {
        // Arrange
        byte[] conteudo = "conteúdo de teste".getBytes();

        // Act
        String resultado = driveApiClientStub.uploadArquivo(
                "arquivo.txt",
                conteudo,
                "text/plain",
                null
        );

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void downloadArquivo_deveRetornarNullSemFazerDownloadReal() {
        // Act
        byte[] resultado = driveApiClientStub.downloadArquivo("file123");

        // Assert
        assertThat(resultado).isNull();
    }

    @Test
    void compartilharArquivo_deveRetornarTrueSemCompartilharReal() {
        // Act
        boolean resultado = driveApiClientStub.compartilharArquivo(
                "file123",
                "test@example.com",
                "reader"
        );

        // Assert
        assertThat(resultado).isTrue();
    }
}
