-- Evolui tabelas propostas e contratos:
-- 1) Adiciona vinculo com servico (FK opcional)
-- 2) Adiciona campos completos na proposta (valor, observacoes, data_validade)
-- 3) Adiciona conteudo binario + metadata para propostas e contratos
-- 4) Torna propostas.proposta_id do contrato nullable para suportar upload direto de contrato

-- ------- PROPOSTAS -------
ALTER TABLE propostas
    ADD COLUMN id_servico INT NULL,
    ADD COLUMN valor DECIMAL(15,2) NULL,
    ADD COLUMN observacoes TEXT NULL,
    ADD COLUMN data_validade DATE NULL,
    ADD COLUMN arquivo_conteudo LONGBLOB NULL,
    ADD COLUMN arquivo_nome VARCHAR(255) NULL,
    ADD COLUMN arquivo_mime VARCHAR(120) NULL,
    ADD COLUMN arquivo_tamanho BIGINT NULL;

ALTER TABLE propostas
    ADD CONSTRAINT fk_propostas_servico
        FOREIGN KEY (id_servico) REFERENCES servicos(id_servico);

-- ------- CONTRATOS -------
-- proposta_id deixa de ser obrigatorio para permitir upload direto de contrato
ALTER TABLE contratos
    MODIFY COLUMN proposta_id INT NULL;

ALTER TABLE contratos
    ADD COLUMN empresa_id INT NULL,
    ADD COLUMN id_servico INT NULL,
    ADD COLUMN arquivo_conteudo LONGBLOB NULL,
    ADD COLUMN arquivo_nome VARCHAR(255) NULL,
    ADD COLUMN arquivo_mime VARCHAR(120) NULL,
    ADD COLUMN arquivo_tamanho BIGINT NULL,
    ADD COLUMN observacoes TEXT NULL;

ALTER TABLE contratos
    ADD CONSTRAINT fk_contratos_servico
        FOREIGN KEY (id_servico) REFERENCES servicos(id_servico),
    ADD CONSTRAINT fk_contratos_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas(id_empresa);

-- Backfill empresa_id dos contratos existentes com a empresa da proposta
UPDATE contratos c
JOIN propostas p ON p.id_proposta = c.proposta_id
SET c.empresa_id = p.empresa_id
WHERE c.empresa_id IS NULL;

-- ------- SEED SERVICOS (catalogo inicial, idempotente) -------
INSERT INTO servicos (nome)
SELECT 'Consultoria Financeira' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Consultoria Financeira');
INSERT INTO servicos (nome)
SELECT 'Gestão de Investimentos' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Gestão de Investimentos');
INSERT INTO servicos (nome)
SELECT 'Planejamento Patrimonial' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Planejamento Patrimonial');
INSERT INTO servicos (nome)
SELECT 'Assessoria Tributária' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Assessoria Tributária');
INSERT INTO servicos (nome)
SELECT 'Auditoria' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Auditoria');
