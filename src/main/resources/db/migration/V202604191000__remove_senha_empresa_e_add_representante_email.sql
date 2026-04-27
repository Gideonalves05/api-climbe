-- Remove a coluna senha_hash da tabela empresas (a empresa contratante não loga no sistema)
-- Afrouxa razao_social (deixa de ser NOT NULL, pois é opcional no cadastro)
-- Adiciona representante_email para contato do representante legal

ALTER TABLE empresas DROP COLUMN senha_hash;

ALTER TABLE empresas MODIFY COLUMN razao_social VARCHAR(255) NULL;

ALTER TABLE empresas ADD COLUMN representante_email VARCHAR(255) NULL;
