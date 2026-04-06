# Documento de Requisitos do Software de Gestão e Gerenciamento de Contratos

**Versão:** 1.0

**Data:** 19/03/2026

# **Introdução**

Este documento apresenta de forma estruturada os requisitos funcionais, não funcionais e de domínio para o desenvolvimento do software de gestão e gerenciamento de contratos solicitado pela **Climbe Investimentos**

# **Escopo**

O sistema cobrirá desde a autenticação dos usuários até o gerenciamento completo dos contratos, documentos, propostas comerciais e reuniões, com integração às APIs do Google Sheets, Drive, Calendar e Gmail. Os usuários terão acesso baseado em cargos e perfis definidos, com permissões específicas atribuídas.

# **Requisitos Funcionais**

1. ## **Autenticação e acesso**

   1. Login por e-mail e senha;

   2. Página de login inicial obrigatória;

2. ## **Autenticação via OAuth 2.0**

   O sistema deve permitir que usuários autentiquem utilizando o protocolo OAuth 2.0, por meio de provedores externos como Google, Microsoft ou outros previamente configurados.

   Ao concluir o processo de autenticação, o sistema deve obter e armazenar com segurança o token de acesso e as informações básicas do perfil do usuário (nome, e-mail, foto de perfil e etc), respeitando as permissões concedidas.

   1. Deve haver opção de login com pelo menos um provedor OAuth preferencialmente o Google;  
   2. O sistema deve redirecionar o usuário para a tela de autorização do provedor;

   3. Após autorização, o sistema deve receber o **access\_token** e buscar os dados do usuário;  
   4. Caso o usuário ainda não exista na base, deve ficar pendente de análise para o administrador aprovar via sistema (o administrador deve ser notificado via e-mail e via sistema);  
   5. Deve haver controle de sessão com base no token recebido;

3. ## **Agenda e Calendário**

   1. Exibição automática da agenda semanal com reuniões, vencimentos e eventos relacionados aos contratos do usuário logado.  
   2. Calendário mensal interativo, permitindo cliques nas datas para agendar reuniões.

   3. Formulário de agendamento de reuniões contendo:

      1. Pauta da reunião;

      2. Empresa (se houver)

      3. Seleção de data e hora com verificação de disponibilidade de participantes e sala (para reuniões presenciais);  
      4. Checkbox indicando se a reunião será presencial ou online;

      5. Notificação por email e no sistema após a confirmação de agendamento;

4. ## **Gestão de Propostas Comerciais**

1. Criação de propostas pelos cargos autorizados: CMO, CSO, CEO, Analista e Contador;  
   2. Aprovação obrigatória pelo contratante:

      1. Proposta aceita: geração automática do contrato pela Compliance;

      2. Proposta recusada: possibilidade de refazer a proposta;

5. ## **Gestão Documental**

   1. Requisição e upload obrigatório dos seguintes documentos por parte da empresa contratante:  
      1. Balanço da Empresa;

      2. Demonstração de Resultados do Exercício (DRE);

      3. Documentos e planilhas gerenciais;

      4. CNPJ;

      5. Contrato Social;

   2. Validação obrigatória dos documentos pelo analista responsável;

6. ## **Gestão de Planilha via Google Sheets**

   1. Integração com Google Sheets utilizando API;

   2. Criação automática de cópia da planilha original para cada contrato;

   3. Segurança das planilhas:

      1. Bloqueio de visualização das fórmulas;

      2. Impedir download da planilha;

      3. Gerenciamento de perfis para visualização e edição;

      4. Exclusão permitida somente na cópia, nunca na planilha original;

7. ## **Gerenciamento de Arquivos no Google Drive**

   1. Uso da API do Google Drive para gerenciamento e restrição de acesso a pastas conforme perfil de usuário;

8. ## **Relatórios e Reuniões**

   1. Anexo	de	documentos	pdf	(relatórios)	após	a	conclusão	do	processo	com possibilidade de fazer download ou visualização;

   2. Agendamento de reunião para apresentação dos relatórios à empresa contratante;

   3. Gestão automática de reuniões via API do Google;

9. ## **Notificações**

   1. Notificações automáticas via email e sistema para:

      1. Alterações no contrato;

      2. Vencimento próximo de toda e qualquer coisa referente aos contratos;

      3. Qualquer andamento pertinente;

10. ## **Cadastro de Empresa**

    1. Formulário de cadastro deve conter os seguintes campos:

       1. Razão Social;

       2. Nome fantasia;

       3. CNPJ;

       4. Endereço completo (logradouro, número, bairro, cidade, UF, CEP);

       5. Telefone e E-mail de contato;

       6. Representante legal (Nome, CPF, contato);

    2. Deve haver verificação de unicidade do CNPJ (não pode haver duplicidade);

    3. O sistema deve exibir mensagens de sucesso ao finalizar o cadastro corretamente;

    4. Em caso de erro, deve exibir mensagem descritiva do problema;

    5. Deve ser possível editar os dados posteriormente, desde que autorizado;

11. ## **Cadastro de usuário**

    1. O formulário de cadastro deve conter os seguintes campos obrigatórios:

       1. Nome completo;

       2. Cargo (selecionado a partir de uma lista pré-cadastrada);

       3. CPF (com validação);

       4. E-mail (podendo ser coorporativo ou não);

       5. Contato;

       6. Situação (ativo/inativo)

    2. O sistema deve permitir a seleção das permissões para o usuário, pode ser várias permissões;  
    3. Deve haver validação para impedir a criação de colaboradores com CPF ou e-mail já cadastrados;  
    4. Ao salvar o cadastro, o colaborador deve receber um e-mail de boas-vindas com as instruções de acesso;  
    5. O cadastro só poderá ser realizado por usuário com perfil administrativo;

# **Requisitos Não Funcionais**

1. ## **Segurança**

1. Autenticação e autorização seguras

   1. Utilizando protocolos modernos e confiáveis, como o OAuth 2.0 com suporte a Authorization Code Flow e PKCE (Proof Key for Code Exchange) quando aplicável;  
   2. Utilização de JWT (JSON Web Token) para controle de sessão e autenticação entre serviços, com expiração e validação segura dos tokens;  
   3. Gerenciamento granular de permissões, com controle de acesso baseado em papéis, garantindo que cada usuário acesse apenas o que lhe é autorizado;  
   4. Proteção contra alterações indevidas em planilhas e documentos originais, por meio do versionamento, permissões de leitura/gravação e trilhas de auditoria;  
   5. Os **tokens** e **dados sensíveis** devem ser armazenados de forma segura, com criptografia em repouso e em trânsito;

2. ## **Desempenho**

   1. Verificação rápida de disponibilidade para agendamento de reuniões;

   2. Carregamento eficiente da agenda e calendário;

3. ## **Confiabilidade**

   1. Garantia de envio das notificações;

   2. Backup regular dos dados manipulados;

4. ## **Usabilidade**

   1. Interface intuitiva, amigável e responsiva;

   2. Claridade na visualização de informações relevantes;

   3. Acessibilidade para modo claro e escuro;

5. ## **Compatibilidade**

   1. Integração completa e eficiente com APIs do Google (Sheets, Drive, Calendar e Gmail);

# **Requisitos de Domínio**

1. ## **Cargos e Perfis**

   1. Compliance;

   2. CEO;

3. Membro do Conselho;

   4. CSO;

   5. CMO;

   6. CFO;

   7. Analista de Valores Imobiliários (Trainee, Junior, Pleno e Sênior);

   8. Analista de BPO Financeiro;

2. ## **Permissões**

   1. Visualização, criação, edição e exclusão de Contratos;

   2. Visualização, criação, edição e exclusão de cargos;

   3. Visualização, criação, edição e exclusão de documentos jurídicos;

   4. Aplicação de nível de complexidade de contratos;

   5. Edição restrita da planilha com necessidade de solicitar permissão;

   6. Agendamento de Reuniões;

   7. Visualização, criação, edição e exclusão de relatórios;

   8. Upload de arquivos;

   9. Download de arquivos;

OBS: poderá haver a concessão de permissões específicas no decorrer do processo.

3. ## **Serviços**

   1. Contabilidade;

   2. Avaliações de Empresas (Valuation);

   3. Terceirização de Rotinas Financeiras (BPO);

   4. Diretoria Financeira Sob Demanda (CFO);

   5. Fusões & Aquisições (M\&A);

As permissões descritas no tópico 2 são aplicáveis para todos os serviços listados acima tal como o fluxo sistêmico permanece o mesmo independente do serviço contratado por empresa terceira.

Este documento apresenta uma visão abrangente dos requisitos necessários para o desenvolvimento do sistema proposto. A adoção desses requisitos funcionais, não funcionais e de domínio garantirá que o produto entregue esteja alinhado com as expectativas dos usuários finais, promovendo maior eficiência operacional, segurança das informações e usabilidade no gerenciamento das atividades relacionadas aos contratos e serviços oferecidos.

Eventuais ajustes ou novas especificações poderão surgir durante o ciclo de desenvolvimento, devendo ser prontamente documentados e incorporados após avaliação do representante da **Climbe Investimentos**.