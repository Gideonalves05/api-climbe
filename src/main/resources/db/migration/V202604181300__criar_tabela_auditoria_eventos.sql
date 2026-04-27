CREATE TABLE auditoria_eventos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento VARCHAR(50) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    entidade_id INT,
    ator_usuario_id INT,
    ator_email VARCHAR(255),
    correlation_id VARCHAR(100),
    payload_json TEXT,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_auditoria_tipo_evento (tipo_evento),
    INDEX idx_auditoria_criado_em (criado_em),
    INDEX idx_auditoria_ator_usuario_id (ator_usuario_id),
    INDEX idx_auditoria_entidade (entidade, entidade_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
