-- Cria tabela para armazenar arquivos binários de propostas (PDF)
CREATE TABLE IF NOT EXISTS proposta_arquivo (
    id_arquivo INT AUTO_INCREMENT PRIMARY KEY,
    proposta_id INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    conteudo LONGBLOB NOT NULL,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_proposta_arquivo_proposta 
        FOREIGN KEY (proposta_id) REFERENCES propostas(id_proposta) 
        ON DELETE CASCADE,
    UNIQUE KEY uk_proposta_arquivo_proposta (proposta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice para consultas por proposta
CREATE INDEX idx_proposta_arquivo_proposta_id ON proposta_arquivo(proposta_id);
