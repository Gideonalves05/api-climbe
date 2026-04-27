package com.climbe.api_climbe.service;

/**
 * Cliente para integração com Google Drive API.
 * Integração desativada por padrão (propriedades GOOGLE_DRIVE_HABILITADO=false).
 */
public interface GoogleDriveApiClient {
    
    /**
     * Faz upload de um arquivo para o Google Drive.
     * 
     * @param nomeArquivo Nome do arquivo
     * @param conteudo Conteúdo do arquivo em bytes
     * @param mimeType MIME type do arquivo
     * @param pastaId ID da pasta no Drive (opcional, usa raiz se null)
     * @return ID do arquivo no Drive, ou null se falhar
     */
    String uploadArquivo(String nomeArquivo, byte[] conteudo, String mimeType, String pastaId);
    
    /**
     * Baixa um arquivo do Google Drive.
     * 
     * @param arquivoId ID do arquivo no Drive
     * @return Conteúdo do arquivo em bytes, ou null se falhar
     */
    byte[] downloadArquivo(String arquivoId);
    
    /**
     * Compartilha um arquivo com um usuário.
     * 
     * @param arquivoId ID do arquivo no Drive
     * @param email Email do usuário para compartilhar
     * @param papel Papel (reader, writer, commenter)
     * @return true se compartilhado com sucesso, false caso contrário
     */
    boolean compartilharArquivo(String arquivoId, String email, String papel);
}
