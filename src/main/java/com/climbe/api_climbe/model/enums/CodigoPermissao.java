package com.climbe.api_climbe.model.enums;

public enum CodigoPermissao {
    // Contratos (5)
    CONTRATO_VER("Visualizar contratos"),
    CONTRATO_CRIAR("Criar contratos"),
    CONTRATO_EDITAR("Editar contratos"),
    CONTRATO_EXCLUIR("Excluir contratos"),
    CONTRATO_DEFINIR_COMPLEXIDADE("Aplicar nível de complexidade"),

    // Propostas (2)
    PROPOSTA_CRIAR("Criar proposta comercial"),
    PROPOSTA_EDITAR("Editar proposta comercial"),

    // Tarefas / Kanban (6)
    TAREFA_VER("Visualizar tarefas de um contrato"),
    TAREFA_CRIAR("Criar tarefa em um contrato"),
    TAREFA_EDITAR("Editar tarefa (título, descrição, prazo, prioridade)"),
    TAREFA_EXCLUIR("Excluir tarefa"),
    TAREFA_MOVER("Mover tarefa entre colunas do Kanban"),
    KANBAN_GERENCIAR_COLUNAS("Criar/editar/remover/reordenar colunas do Kanban do contrato"),
    CONTRATO_INTERAGIR_QUALQUER("Interagir (ver/mover/editar) no Kanban de qualquer contrato, sem depender do time"),

    // Time do contrato (3)
    TIME_CONTRATO_VER("Visualizar membros do time de um contrato"),
    TIME_CONTRATO_ADICIONAR("Adicionar membro ao time"),
    TIME_CONTRATO_REMOVER("Remover membro do time"),

    // Documentos (3)
    DOCUMENTO_VALIDAR("Validar / reprovar documentos da contratante"),
    DOCUMENTO_SOLICITAR_ADICIONAL("Solicitar documentos adicionais à contratante"),
    DOCUMENTO_JURIDICO_GERENCIAR("Gerenciar documentos jurídicos (contratos assinados, anexos legais)"),

    // Planilha e arquivos (3)
    PLANILHA_EDITAR_RESTRITA("Editar planilha do contrato (modo restrito, sem fórmulas)"),
    ARQUIVO_UPLOAD("Upload de arquivos"),
    ARQUIVO_DOWNLOAD("Download de arquivos"),

    // Reuniões (1)
    REUNIAO_AGENDAR("Agendar reuniões"),

    // Relatórios (4)
    RELATORIO_VER("Visualizar relatórios"),
    RELATORIO_CRIAR("Criar relatórios"),
    RELATORIO_EDITAR("Editar relatórios"),
    RELATORIO_EXCLUIR("Excluir relatórios"),

    // Administração (10)
    USUARIO_GERENCIAR("Criar / editar / desativar usuários internos"),
    USUARIO_APROVAR("Aprovar usuários pendentes vindos de OAuth"),
    USUARIO_LISTAR_PENDENTES("Listar usuários pendentes de aprovação"),
    CARGO_GERENCIAR("Criar / editar / remover cargos"),
    EMPRESA_GERENCIAR("Criar / editar dados de empresas contratantes"),
    EMPRESA_EDITAR("Criar e editar empresas contratantes pela UI"),
    SERVICO_EDITAR("Criar e editar serviços no catálogo"),
    NOTIFICACAO_ADMIN("Acessar outbox, refazer envios, troubleshooting de notificações"),
    AUDITORIA_VER("Consultar trilha de auditoria de eventos do sistema"),
    AUDITORIA_EXPORTAR("Exportar trilha de auditoria em CSV/PDF");

    private final String descricaoPadrao;

    CodigoPermissao(String descricaoPadrao) {
        this.descricaoPadrao = descricaoPadrao;
    }

    public String getDescricaoPadrao() {
        return descricaoPadrao;
    }
}
