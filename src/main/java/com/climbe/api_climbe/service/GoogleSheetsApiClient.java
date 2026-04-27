package com.climbe.api_climbe.service;

/**
 * Cliente para integração com Google Sheets API.
 * Integração desativada por padrão (propriedades GOOGLE_SHEETS_HABILITADO=false).
 */
public interface GoogleSheetsApiClient {
    
    /**
     * Cria uma nova planilha no Google Sheets.
     * 
     * @param titulo Título da planilha
     * @return ID da planilha criada, ou null se falhar
     */
    String criarPlanilha(String titulo);
    
    /**
     * Lê dados de uma planilha.
     * 
     * @param spreadsheetId ID da planilha
     * @param intervalo Intervalo no formato A1 (ex: "Sheet1!A1:D10")
     * @return Matriz de valores (linhas x colunas), ou null se falhar
     */
    Object[][] lerDados(String spreadsheetId, String intervalo);
    
    /**
     * Escreve dados em uma planilha.
     * 
     * @param spreadsheetId ID da planilha
     * @param intervalo Intervalo no formato A1 (ex: "Sheet1!A1:D10")
     * @param valores Matriz de valores para escrever
     * @return true se escrito com sucesso, false caso contrário
     */
    boolean escreverDados(String spreadsheetId, String intervalo, Object[][] valores);
    
    /**
     * Adiciona uma linha a uma planilha.
     * 
     * @param spreadsheetId ID da planilha
     * @param nomeAba Nome da aba
     * @param valores Array de valores para a linha
     * @return true se adicionado com sucesso, false caso contrário
     */
    boolean adicionarLinha(String spreadsheetId, String nomeAba, Object[] valores);
}
