-- Tornar campo codigo NOT NULL e UNIQUE após backfill do PermissaoSeeder
-- Migration para finalizar catálogo de permissões com código único

-- Passo 1: Remover índice temporário
DROP INDEX IF EXISTS idx_permissoes_codigo ON permissoes;

-- Passo 2: Tornar codigo NOT NULL (assume que PermissaoSeeder já preencheu todos os registros)
ALTER TABLE permissoes MODIFY COLUMN codigo VARCHAR(60) NOT NULL;

-- Passo 3: Adicionar constraint UNIQUE
ALTER TABLE permissoes ADD CONSTRAINT uk_permissoes_codigo UNIQUE (codigo);
