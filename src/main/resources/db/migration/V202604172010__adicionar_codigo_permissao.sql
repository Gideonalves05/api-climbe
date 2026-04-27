-- Adicionar campo codigo na tabela permissoes
-- Migration para suportar catálogo de permissões com código único

-- Passo 1: Adicionar coluna codigo como nullable inicialmente
ALTER TABLE permissoes ADD COLUMN codigo VARCHAR(60) NULL;

-- Passo 2: Criar índice para performance (será unique após backfill)
CREATE INDEX idx_permissoes_codigo ON permissoes(codigo);

-- NOTA: O backfill dos dados existentes será feito pelo PermissaoSeeder
-- Após o backfill e validação, será necessário uma migration adicional para:
-- 1. ALTER TABLE permissoes MODIFY COLUMN codigo VARCHAR(60) NOT NULL;
-- 2. ALTER TABLE permissoes ADD CONSTRAINT uk_permissoes_codigo UNIQUE (codigo);
