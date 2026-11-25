# language: pt
# =====================================================================================
# COMO LER ESTE ARQUIVO (para quem está chegando ao projeto)
#
# - "Contexto" roda antes de CADA cenário deste arquivo: aqui ele cria a tarefa usada
#   pelos dois cenários. Cada cenário recebe a sua própria tarefa, criada do zero.
# - "Regra" agrupa os cenários pela regra de negócio que eles comprovam.
# - Os comentários numerados (# 1., # 2. ...) separam as etapas da jornada: cada uma é
#   um Quando (ação) seguido de um Então (validação).
# - O código de cada passo fica em src/test/java/steps (Ctrl+clique na frase, no IntelliJ).
# - Executar só esta funcionalidade: mvn test -Dcucumber.filter.tags="@ciclo-vida"
# - No Allure, cada passo traz o print da tela e o log do que foi feito.
# =====================================================================================
@ciclo-vida
Funcionalidade: Ciclo de vida da tarefa
  Para manter a lista fiel ao andamento do trabalho
  Como pessoa que organiza o próprio trabalho
  Quero editar, concluir, reabrir e excluir minhas tarefas

  # Cada cenário percorre uma jornada completa sobre a mesma tarefa, como no uso real do app:
  # a massa é criada uma única vez por cenário e cada etapa parte do resultado da anterior.
  # A tarefa criada aqui é excluída automaticamente ao final (hook de limpeza), mesmo se o
  # cenário falhar.
  Contexto:
    Dado que existe a tarefa "Corrigir bug no login"

  Regra: Alterações na tarefa só são gravadas ao salvar

    # Três títulos provam o que foi gravado e o que foi descartado:
    #   "Corrigir bug no login"                   -> título original
    #   "Corrigir bug no login do app"            -> rascunho, nunca deve chegar à lista
    #   "Corrigir bug no login e publicar hotfix" -> alteração salva
    @severity=critical
    Cenário: Editar a tarefa, descartar o rascunho e salvar o novo título
      # 1. Sair com alteração pendente: o app não sai direto, pede confirmação
      Quando eu altero o título da tarefa "Corrigir bug no login" para "Corrigir bug no login do app" sem salvar
      E eu tento sair da edição
      Então o aplicativo pede a confirmação para descartar as alterações

      # 2. "Continuar editando" fecha o diálogo e preserva o que foi digitado
      Quando eu escolho continuar editando
      Então o título em edição continua "Corrigir bug no login do app"

      # 3. "Descartar" fecha a edição sem gravar: a lista mantém o título original
      Quando eu tento sair da edição
      E eu escolho descartar as alterações
      Então a tarefa "Corrigir bug no login" aparece na lista
      E a tarefa "Corrigir bug no login do app" não aparece na lista

      # 4. Salvar grava o novo título e substitui o original na lista
      Quando eu altero o título da tarefa "Corrigir bug no login" para "Corrigir bug no login e publicar hotfix" e salvo
      Então a tarefa "Corrigir bug no login e publicar hotfix" aparece na lista
      E a tarefa "Corrigir bug no login" não aparece na lista

  Regra: Tarefas concluídas podem ser ocultadas, exibidas, reabertas e excluídas

    # A opção "Mostrar tarefas concluídas" decide o destino da tarefa concluída: sumir da lista ou
    # ir para o grupo "Concluída". Se o cenário ocultar as concluídas, a limpeza volta a exibi-las.
    @severity=critical
    Cenário: Concluir, reabrir e excluir a tarefa
      # 1. Com as concluídas ocultas, concluir tira a tarefa da lista
      Dado que as tarefas concluídas estão ocultas
      Quando eu concluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" deixa de aparecer na lista

      # 2. Ao exibir as concluídas, a tarefa reaparece no grupo "Concluída"
      Quando eu passo a exibir as tarefas concluídas
      Então a tarefa "Corrigir bug no login" aparece no grupo "Concluída"

      # 3. Reabrir devolve a tarefa às ativas (ela não tem data, por isso esse grupo)
      Quando eu reabro a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" aparece no grupo "Sem data de vencimento"

      # 4. Excluir é definitivo: a tarefa some da lista e também da busca
      Quando eu excluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" não aparece na lista
      E a busca por "Corrigir bug no login" não encontra tarefas
