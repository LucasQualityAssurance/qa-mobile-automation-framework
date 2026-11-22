# language: pt
@ciclo-vida
Funcionalidade: Ciclo de vida da tarefa
  Para manter a lista fiel ao andamento do trabalho
  Como pessoa que organiza o próprio trabalho
  Quero editar, concluir, reabrir e excluir minhas tarefas

  Contexto:
    Dado que existe a tarefa "Corrigir bug no login"

  Regra: Alterações na tarefa só são gravadas ao salvar

    @severity=critical
    Cenário: Sair da edição com alterações pendentes pede confirmação
      Dado que alterei o título da tarefa "Corrigir bug no login" para "Corrigir bug no login do app" sem salvar
      Quando eu tento sair da edição
      Então o aplicativo pede a confirmação para descartar as alterações

    @severity=critical
    Cenário: Continuar editando mantém a alteração pendente
      Dado que alterei o título da tarefa "Corrigir bug no login" para "Corrigir bug no login do app" sem salvar
      E que tentei sair da edição
      Quando eu escolho continuar editando
      Então o título em edição continua "Corrigir bug no login do app"

    @severity=critical
    Cenário: Descartar as alterações mantém o título original
      Dado que alterei o título da tarefa "Corrigir bug no login" para "Corrigir bug no login do app" sem salvar
      E que tentei sair da edição
      Quando eu escolho descartar as alterações
      Então a tarefa "Corrigir bug no login" aparece na lista
      E a tarefa "Corrigir bug no login do app" não aparece na lista

    @severity=critical
    Cenário: Salvar o novo título da tarefa
      Quando eu altero o título da tarefa "Corrigir bug no login" para "Corrigir bug no login e publicar hotfix" e salvo
      Então a tarefa "Corrigir bug no login e publicar hotfix" aparece na lista
      E a tarefa "Corrigir bug no login" não aparece na lista

  Regra: Tarefas concluídas podem ser ocultadas ou exibidas na lista

    @severity=critical
    Cenário: Concluir uma tarefa com as concluídas ocultas
      Dado que as tarefas concluídas estão ocultas
      Quando eu concluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" deixa de aparecer na lista

    @severity=critical
    Cenário: Concluir uma tarefa com as concluídas exibidas
      Dado que as tarefas concluídas estão sendo exibidas
      Quando eu concluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" aparece no grupo "Concluída"

    @severity=critical
    Cenário: Reabrir uma tarefa concluída
      Dado que a tarefa "Corrigir bug no login" está concluída
      Quando eu reabro a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" aparece no grupo "Sem data de vencimento"

  Regra: Tarefas excluídas são removidas definitivamente

    @severity=critical
    Cenário: Excluir uma tarefa
      Quando eu excluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" não aparece na lista
      E a busca por "Corrigir bug no login" não encontra tarefas
