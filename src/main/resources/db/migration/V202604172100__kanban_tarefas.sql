-- Migration para Kanban e Tarefas por Contrato
-- Criar tabelas: membros_time, colunas_kanban, tarefas_contrato, tarefa_dependencias, 
-- tarefa_anexos, tarefa_comentarios, tarefa_checklist_itens, tarefa_co_responsaveis, tarefa_observadores

-- Tabela membros_time
CREATE TABLE membros_time (
    id_membro_time INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    usuario_id INT NOT NULL,
    papel ENUM('LIDER', 'MEMBRO') NOT NULL DEFAULT 'MEMBRO',
    data_entrada DATE NOT NULL DEFAULT (CURRENT_DATE),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id_contrato) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    UNIQUE KEY uk_membros_time_contrato_usuario_ativo (contrato_id, usuario_id, ativo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela colunas_kanban
CREATE TABLE colunas_kanban (
    id_coluna INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    nome VARCHAR(60) NOT NULL,
    ordem INT NOT NULL,
    tipo ENUM('INICIAL', 'INTERMEDIARIA', 'FINAL') NOT NULL,
    cor VARCHAR(7),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id_contrato) ON DELETE CASCADE,
    UNIQUE KEY uk_colunas_kanban_contrato_ordem (contrato_id, ordem)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefas_contrato
CREATE TABLE tarefas_contrato (
    id_tarefa INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    coluna_id INT NOT NULL,
    titulo VARCHAR(160) NOT NULL,
    descricao TEXT,
    prioridade ENUM('BAIXA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL DEFAULT 'MEDIA',
    responsavel_principal_id INT NOT NULL,
    data_limite DATETIME NOT NULL,
    data_conclusao DATETIME,
    criado_por_id INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id_contrato) ON DELETE CASCADE,
    FOREIGN KEY (coluna_id) REFERENCES colunas_kanban(id_coluna) ON DELETE RESTRICT,
    FOREIGN KEY (responsavel_principal_id) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    FOREIGN KEY (criado_por_id) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    INDEX idx_tarefas_contrato_contrato (contrato_id),
    INDEX idx_tarefas_contrato_coluna (coluna_id),
    INDEX idx_tarefas_contrato_responsavel (responsavel_principal_id),
    INDEX idx_tarefas_contrato_data_limite (data_limite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_co_responsaveis (N:N)
CREATE TABLE tarefa_co_responsaveis (
    tarefa_id INT NOT NULL,
    usuario_id INT NOT NULL,
    adicionado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tarefa_id, usuario_id),
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_observadores (N:N)
CREATE TABLE tarefa_observadores (
    tarefa_id INT NOT NULL,
    usuario_id INT NOT NULL,
    adicionado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tarefa_id, usuario_id),
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_dependencias (auto-relacional N:N)
CREATE TABLE tarefa_dependencias (
    id_dependencia INT AUTO_INCREMENT PRIMARY KEY,
    tarefa_id INT NOT NULL,
    depende_de_id INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    FOREIGN KEY (depende_de_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    UNIQUE KEY uk_tarefa_dependencias (tarefa_id, depende_de_id),
    CONSTRAINT chk_tarefa_dependencias_diferente CHECK (tarefa_id != depende_de_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_anexos
CREATE TABLE tarefa_anexos (
    id_anexo INT AUTO_INCREMENT PRIMARY KEY,
    tarefa_id INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_mime VARCHAR(100),
    tamanho_bytes BIGINT,
    drive_file_id VARCHAR(120),
    url_acesso VARCHAR(500),
    upload_por_id INT NOT NULL,
    upload_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    FOREIGN KEY (upload_por_id) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    INDEX idx_tarefa_anexos_tarefa (tarefa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_comentarios
CREATE TABLE tarefa_comentarios (
    id_comentario INT AUTO_INCREMENT PRIMARY KEY,
    tarefa_id INT NOT NULL,
    autor_id INT NOT NULL,
    texto TEXT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editado_em TIMESTAMP NULL,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    FOREIGN KEY (autor_id) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    INDEX idx_tarefa_comentarios_tarefa (tarefa_id),
    INDEX idx_tarefa_comentarios_criado_em (criado_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela tarefa_checklist_itens
CREATE TABLE tarefa_checklist_itens (
    id_item INT AUTO_INCREMENT PRIMARY KEY,
    tarefa_id INT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    concluido BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    INDEX idx_tarefa_checklist_itens_tarefa (tarefa_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
