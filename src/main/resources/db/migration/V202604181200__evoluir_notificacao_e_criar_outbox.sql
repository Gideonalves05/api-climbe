-- Evolução do modelo de notificações para suportar Outbox pattern, SSE e preferências
-- Referência: documentos/estrategia-notificacoes.md

-- 1. Evoluir tabela notificacoes com novos campos
ALTER TABLE notificacoes
    ADD COLUMN titulo VARCHAR(120) NULL COMMENT 'Título da notificação (ex: Nova tarefa atribuída)',
    ADD COLUMN link_destino VARCHAR(255) NULL COMMENT 'Deeplink para UI (ex: /contratos/12/tarefas/345)',
    ADD COLUMN payload TEXT NULL COMMENT 'Metadados estruturados em JSON para renderização rica',
    ADD COLUMN lida BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Indica se notificação foi lida pelo usuário',
    ADD COLUMN lida_em DATETIME NULL COMMENT 'Timestamp quando notificação foi marcada como lida',
    ADD COLUMN criado_em DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp de criação da notificação';

-- data_envio continua por compatibilidade, mas será descontinuada em migração futura

-- 2. Criar tabela notificacoes_outbox (registro de tentativa de entrega por canal)
CREATE TABLE notificacoes_outbox (
    id_outbox BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_notificacao INT NOT NULL,
    canal VARCHAR(20) NOT NULL COMMENT 'IN_APP, EMAIL, SSE',
    destino VARCHAR(255) NOT NULL COMMENT 'Email resolvido ou userId (para SSE/IN_APP)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' COMMENT 'PENDENTE, ENVIADA, ERRO_PERMANENTE',
    tentativas INT DEFAULT 0 NOT NULL,
    max_tentativas INT DEFAULT 6 NOT NULL,
    proxima_tentativa DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_tentativa_em DATETIME NULL,
    ultimo_erro VARCHAR(500) NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_outbox_notificacao FOREIGN KEY (id_notificacao) REFERENCES notificacoes(id_notificacao) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para o dispatcher
CREATE INDEX idx_outbox_status_proxima ON notificacoes_outbox(status, proxima_tentativa);
CREATE INDEX idx_outbox_notificacao ON notificacoes_outbox(id_notificacao);

-- 3. Criar tabela preferencias_notificacao (opt-out granular por usuário/tipo/canal)
CREATE TABLE preferencias_notificacao (
    id_preferencia INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo VARCHAR(40) NOT NULL COMMENT 'Tipo de notificação (TipoNotificacao)',
    canal VARCHAR(20) NOT NULL COMMENT 'IN_APP, EMAIL, SSE',
    habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pref_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT uk_usuario_tipo_canal UNIQUE (id_usuario, tipo, canal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
