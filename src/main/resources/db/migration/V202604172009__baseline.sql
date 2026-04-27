-- Baseline do schema atual do banco de dados
-- Gerado em 17/04/2026 20:09
-- Reflete as entidades JPA existentes antes da adoção do Flyway

-- Tabela: cargos
CREATE TABLE cargos (
    id_cargo INT AUTO_INCREMENT PRIMARY KEY,
    nome_cargo VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: servicos
CREATE TABLE servicos (
    id_servico INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: empresas
CREATE TABLE empresas (
    id_empresa INT AUTO_INCREMENT PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    logradouro VARCHAR(255),
    numero VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    uf VARCHAR(2),
    cep VARCHAR(9),
    telefone VARCHAR(50),
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(60) NOT NULL,
    representante_nome VARCHAR(255),
    representante_cpf VARCHAR(14),
    representante_contato VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: empresa_servico (N:N)
CREATE TABLE empresa_servico (
    id_empresa INT NOT NULL,
    id_servico INT NOT NULL,
    PRIMARY KEY (id_empresa, id_servico),
    FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa) ON DELETE CASCADE,
    FOREIGN KEY (id_servico) REFERENCES servicos(id_servico) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: permissoes
CREATE TABLE permissoes (
    id_permissao INT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(255) NOT NULL,
    cargo_id INT NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    contato VARCHAR(255),
    situacao VARCHAR(20) NOT NULL,
    senha_hash VARCHAR(60) NOT NULL,
    FOREIGN KEY (cargo_id) REFERENCES cargos(id_cargo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: usuario_permissoes (N:N)
CREATE TABLE usuario_permissoes (
    id_usuario INT NOT NULL,
    id_permissao INT NOT NULL,
    PRIMARY KEY (id_usuario, id_permissao),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_permissao) REFERENCES permissoes(id_permissao) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: propostas
CREATE TABLE propostas (
    id_proposta INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT NOT NULL,
    usuario_id INT NOT NULL,
    status VARCHAR(40) NOT NULL,
    documento_proposta TEXT,
    data_criacao DATE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id_empresa),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: contratos
CREATE TABLE contratos (
    id_contrato INT AUTO_INCREMENT PRIMARY KEY,
    proposta_id INT NOT NULL UNIQUE,
    data_inicio DATE,
    data_fim DATE,
    status VARCHAR(40),
    FOREIGN KEY (proposta_id) REFERENCES propostas(id_proposta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: planilhas
CREATE TABLE planilhas (
    id_planilha INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    url_google_sheets VARCHAR(255),
    bloqueada BOOLEAN,
    permissao_visualizacao VARCHAR(255),
    FOREIGN KEY (contrato_id) REFERENCES contratos(id_contrato)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: relatorios
CREATE TABLE relatorios (
    id_relatorio INT AUTO_INCREMENT PRIMARY KEY,
    contrato_id INT NOT NULL,
    url_pdf VARCHAR(255),
    data_envio DATE,
    FOREIGN KEY (contrato_id) REFERENCES contratos(id_contrato)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: reunioes
CREATE TABLE reunioes (
    id_reuniao INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    empresa_id INT,
    data DATE,
    hora TIME,
    presencial BOOLEAN,
    local VARCHAR(255),
    pauta TEXT,
    status VARCHAR(40),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: participantes_reuniao (N:N)
CREATE TABLE participantes_reuniao (
    id_reuniao INT NOT NULL,
    id_usuario INT NOT NULL,
    PRIMARY KEY (id_reuniao, id_usuario),
    FOREIGN KEY (id_reuniao) REFERENCES reunioes(id_reuniao) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: documentos
CREATE TABLE documentos (
    id_documento INT AUTO_INCREMENT PRIMARY KEY,
    empresa_id INT NOT NULL,
    tipo_documento VARCHAR(60) NOT NULL,
    url VARCHAR(255),
    validado VARCHAR(30),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela: notificacoes
CREATE TABLE notificacoes (
    id_notificacao INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    mensagem VARCHAR(255) NOT NULL,
    data_envio DATE,
    tipo VARCHAR(40),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
