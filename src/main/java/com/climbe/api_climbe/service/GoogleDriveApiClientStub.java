package com.climbe.api_climbe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementação stub do GoogleDriveApiClient.
 * Loga a ação sem fazer upload/download real.
 * Ativa quando google.integracoes.drive.habilitado=false (default)
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "google.integracoes.drive", name = "habilitado", havingValue = "false", matchIfMissing = true)
public class GoogleDriveApiClientStub implements GoogleDriveApiClient {

    @Override
    public String uploadArquivo(String nomeArquivo, byte[] conteudo, String mimeType, String pastaId) {
        log.info("[DRIVE STUB] Arquivo não enviado (integração desativada): nome={}, tamanho={} bytes", 
                 nomeArquivo, conteudo != null ? conteudo.length : 0);
        return null;
    }

    @Override
    public byte[] downloadArquivo(String arquivoId) {
        log.info("[DRIVE STUB] Arquivo não baixado (integração desativada): arquivoId={}", arquivoId);
        return null;
    }

    @Override
    public boolean compartilharArquivo(String arquivoId, String email, String papel) {
        log.info("[DRIVE STUB] Arquivo não compartilhado (integração desativada): arquivoId={}, email={}, papel={}", 
                 arquivoId, email, papel);
        return true;
    }
}
