package com.climbe.api_climbe.model.enums;

public enum TipoNotificacao {
    // Tipos existentes
    ALTERACAO_CONTRATO,
    VENCIMENTO,
    ANDAMENTO,
    APROVACAO_USUARIO,
    SOLICITACAO_ACESSO_PENDENTE,
    REUNIAO,

    // Tipos para Kanban
    TAREFA_CRIADA,
    TAREFA_ATRIBUIDA,
    TAREFA_MOVIDA,
    TAREFA_COMENTARIO,
    TAREFA_ANEXO,
    TAREFA_DEPENDENCIA,
    TAREFA_PRAZO_PROXIMO,
    TAREFA_VENCIDA,

    // Tipos para propostas
    PROPOSTA_ACEITA,
    PROPOSTA_RECUSADA,

    // Tipos para documentos
    DOCUMENTO_SOLICITADO,
    DOCUMENTO_VALIDADO,
    DOCUMENTO_REPROVADO
}
