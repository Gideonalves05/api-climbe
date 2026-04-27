-- Saneamento idempotente do schema de empresas
-- Garante o schema correto mesmo em bases que foram inicialmente criadas
-- pelo Hibernate (ddl-auto=update) antes do Flyway estar ativo.
--
-- Idempotente: usa information_schema para checar antes de executar DDL.

-- Remove coluna senha_hash se existir (empresa não autentica mais)
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'empresas'
      AND COLUMN_NAME = 'senha_hash'
);
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE empresas DROP COLUMN senha_hash',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Garante razao_social como NULL (opcional)
SET @is_not_null = (
    SELECT IS_NULLABLE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'empresas'
      AND COLUMN_NAME = 'razao_social'
);
SET @sql = IF(@is_not_null = 'NO',
    'ALTER TABLE empresas MODIFY COLUMN razao_social VARCHAR(255) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Garante representante_email
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'empresas'
      AND COLUMN_NAME = 'representante_email'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE empresas ADD COLUMN representante_email VARCHAR(255) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
