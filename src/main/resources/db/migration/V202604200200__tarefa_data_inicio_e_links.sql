-- Adiciona prazo de início e tabela de links para as tarefas do Kanban
ALTER TABLE tarefas_contrato
    ADD COLUMN data_inicio DATETIME NULL AFTER prioridade;

-- Afrouxa obrigatoriedade da data_limite (pode ser nula quando a tarefa ainda não tem prazo)
ALTER TABLE tarefas_contrato
    MODIFY COLUMN data_limite DATETIME NULL;

CREATE TABLE IF NOT EXISTS tarefa_links (
    id_link INT AUTO_INCREMENT PRIMARY KEY,
    tarefa_id INT NOT NULL,
    url VARCHAR(500) NOT NULL,
    titulo VARCHAR(160) NULL,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tarefa_links_tarefa FOREIGN KEY (tarefa_id) REFERENCES tarefas_contrato(id_tarefa) ON DELETE CASCADE,
    INDEX idx_tarefa_links_tarefa (tarefa_id)
);
