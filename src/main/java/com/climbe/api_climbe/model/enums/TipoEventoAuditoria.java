package com.climbe.api_climbe.model.enums;

public enum TipoEventoAuditoria {
    CONTRATO_CRIADO("Contrato criado"),
    CONTRATO_EDITADO("Contrato editado"),
    CONTRATO_EXCLUIDO("Contrato excluído"),
    
    PROPOSTA_CRIADA("Proposta criada"),
    PROPOSTA_EDITADA("Proposta editada"),
    PROPOSTA_DECISAO_REGISTRADA("Decisão de proposta registrada"),
    
    TAREFA_CRIADA("Tarefa criada"),
    TAREFA_EDITADA("Tarefa editada"),
    TAREFA_EXCLUIDA("Tarefa excluída"),
    TAREFA_MOVIDA("Tarefa movida entre colunas"),
    TAREFA_CONCLUIDA("Tarefa concluída"),
    
    MEMBRO_TIME_ADICIONADO("Membro adicionado ao time"),
    MEMBRO_TIME_REMOVIDO("Membro removido do time"),
    
    DOCUMENTO_VALIDADO("Documento validado"),
    DOCUMENTO_REPROVADO("Documento reprovado"),
    
    USUARIO_CRIADO("Usuário criado"),
    USUARIO_EDITADO("Usuário editado"),
    USUARIO_DESATIVADO("Usuário desativado"),
    SOLICITACAO_ACESSO_CRIADA("Solicitação de acesso criada"),
    USUARIO_APROVADO("Usuário aprovado"),
    USUARIO_REJEITADO("Usuário rejeitado"),
    USUARIO_ATIVADO("Usuário ativado"),
    INTEGRACAO_GMAIL_FALLBACK("Integração Gmail Fallback"),
    USUARIO_PERMISSAO_ATRIBUIDA("Permissão atribuída ao usuário"),
    USUARIO_PERMISSAO_REMOVIDA("Permissão removida do usuário"),
    
    NOTIFICACAO_ENVIADA("Notificação enviada"),
    NOTIFICACAO_LIDA("Notificação lida"),
    
    LOGIN_REALIZADO("Login realizado"),
    LOGOUT_REALIZADO("Logout realizado"),
    
    REUNIAO_AGENDADA("Reunião agendada"),
    REUNIAO_EDITADA("Reunião editada"),
    REUNIAO_CANCELADA("Reunião cancelada"),

    EMPRESA_CRIADA("Empresa criada"),
    EMPRESA_ATUALIZADA("Empresa atualizada"),

    KANBAN_INICIALIZADO("Kanban inicializado");

    private final String descricao;

    TipoEventoAuditoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
