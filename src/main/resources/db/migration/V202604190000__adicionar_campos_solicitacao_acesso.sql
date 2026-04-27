-- Adicionar campos para fluxo de solicitação de acesso
-- Task 8.0: Fluxo de autenticação e solicitação de acesso de novos usuários

ALTER TABLE usuarios 
ADD COLUMN token_ativacao VARCHAR(64) NULL COMMENT 'Token de ativação de conta (hash SHA-256)',
ADD COLUMN token_expira_em DATETIME NULL COMMENT 'Data/hora de expiração do token de ativação',
ADD COLUMN motivo_rejeicao VARCHAR(500) NULL COMMENT 'Motivo da rejeição da solicitação de acesso',
ADD COLUMN criado_em DATETIME NULL COMMENT 'Data/hora de criação do registro';

-- Adicionar índice para busca por token de ativação
CREATE INDEX idx_usuarios_token_ativacao ON usuarios(token_ativacao);

-- Backfill criado_em para registros existentes
UPDATE usuarios SET criado_em = NOW() WHERE criado_em IS NULL;
