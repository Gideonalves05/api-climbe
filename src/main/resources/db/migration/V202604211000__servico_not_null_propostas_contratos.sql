-- Torna o vínculo de serviço obrigatório (NOT NULL) em propostas e contratos
-- Strategy: criar serviço fallback, atualizar registros NULL, alterar coluna para NOT NULL

-- 1. Criar serviço fallback "Não classificado" se não existir
INSERT INTO servicos (nome)
SELECT 'Não classificado' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Não classificado');

-- 2. Obter o ID do serviço fallback para usar nos updates
SET @fallback_id = (SELECT id_servico FROM servicos WHERE nome = 'Não classificado' LIMIT 1);

-- 3. Atualizar propostas existentes com servico_id NULL para o fallback
UPDATE propostas
SET id_servico = @fallback_id
WHERE id_servico IS NULL;

-- 4. Atualizar contratos existentes com servico_id NULL para o fallback
UPDATE contratos
SET id_servico = @fallback_id
WHERE id_servico IS NULL;

-- 5. Tornar as colunas NOT NULL
ALTER TABLE propostas
    MODIFY COLUMN id_servico INT NOT NULL,
    DROP FOREIGN KEY IF EXISTS fk_propostas_servico,
    ADD CONSTRAINT fk_propostas_servico
        FOREIGN KEY (id_servico) REFERENCES servicos(id_servico);

ALTER TABLE contratos
    MODIFY COLUMN id_servico INT NOT NULL,
    DROP FOREIGN KEY IF EXISTS fk_contratos_servico,
    ADD CONSTRAINT fk_contratos_servico
        FOREIGN KEY (id_servico) REFERENCES servicos(id_servico);
