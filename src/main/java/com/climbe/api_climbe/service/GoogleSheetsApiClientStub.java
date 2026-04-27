package com.climbe.api_climbe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementação stub do GoogleSheetsApiClient.
 * Loga a ação sem acessar planilha real.
 * Ativa quando google.integracoes.sheets.habilitado=false (default)
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "google.integracoes.sheets", name = "habilitado", havingValue = "false", matchIfMissing = true)
public class GoogleSheetsApiClientStub implements GoogleSheetsApiClient {

    @Override
    public String criarPlanilha(String titulo) {
        log.info("[SHEETS STUB] Planilha não criada (integração desativada): titulo={}", titulo);
        return null;
    }

    @Override
    public Object[][] lerDados(String spreadsheetId, String intervalo) {
        log.info("[SHEETS STUB] Dados não lidos (integração desativada): spreadsheetId={}, intervalo={}", 
                 spreadsheetId, intervalo);
        return null;
    }

    @Override
    public boolean escreverDados(String spreadsheetId, String intervalo, Object[][] valores) {
        log.info("[SHEETS STUB] Dados não escritos (integração desativada): spreadsheetId={}, intervalo={}, linhas={}", 
                 spreadsheetId, intervalo, valores != null ? valores.length : 0);
        return true;
    }

    @Override
    public boolean adicionarLinha(String spreadsheetId, String nomeAba, Object[] valores) {
        log.info("[SHEETS STUB] Linha não adicionada (integração desativada): spreadsheetId={}, nomeAba={}, colunas={}", 
                 spreadsheetId, nomeAba, valores != null ? valores.length : 0);
        return true;
    }
}
