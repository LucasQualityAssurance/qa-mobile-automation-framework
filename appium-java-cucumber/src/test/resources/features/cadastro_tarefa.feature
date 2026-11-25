# language: pt
# =====================================================================================
# COMO LER ESTE ARQUIVO (para quem está chegando ao projeto)
#
# - Cada linha que começa com Dado/Quando/Então/E é um passo. O código de cada passo
#   fica em src/test/java/steps: procure a frase (sem os valores entre aspas) na
#   anotação @Dado/@Quando/@Então. No IntelliJ, Ctrl+clique na frase abre o método.
#     Dado   -> prepara a massa (estado inicial)
#     Quando -> a ação que está sendo testada
#     Então  -> a validação do resultado esperado
# - Os valores entre aspas e as tabelas são os dados do teste: troque-os aqui, sem
#   mexer no Java.
# - Tags (@cadastro, @smoke...) servem para filtrar a execução:
#     mvn test -Dcucumber.filter.tags="@cadastro"   (só esta funcionalidade)
#     mvn test -Psmoke                              (só os cenários @smoke)
#   @severity=... define a severidade exibida no relatório Allure.
# - Antes de cada cenário o app é levado para "Minhas tarefas"; ao final, as tarefas
#   criadas são excluídas (hooks/Ganchos.java). Os cenários não dependem uns dos outros.
# - No Allure, cada passo traz o print da tela e o log do que foi feito
#   (mvn allure:serve para abrir o relatório).
# =====================================================================================
@cadastro
Funcionalidade: Cadastro de tarefa
  Para acompanhar as demandas de cada projeto de T.I.
  Como pessoa que organiza o próprio trabalho
  Quero cadastrar tarefas com vencimento, prioridade, lista e subtarefas

  # Cenário ponta a ponta do cadastro: preenche todos os campos principais e confere a tarefa em
  # cada lugar onde ela deve aparecer. Marcado como @smoke: roda também em "mvn test -Psmoke".
  # A tarefa é excluída ao final pela limpeza; a lista "Projetos de TI" é reaproveitada.
  @smoke @severity=blocker
  Cenário: Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa
    # A lista é criada só se ainda não existir no menu lateral
    Dado que existe a lista "Projetos de TI"
    # Campos da tabela (todos opcionais): vencimento, prioridade, lista e subtarefa
    Quando eu cadastro a tarefa "Configurar pipeline de CI/CD" com os dados:
      | vencimento | hoje                             |
      | prioridade | alta                             |
      | lista      | Projetos de TI                   |
      | subtarefa  | Criar workflow no GitHub Actions |
    # Na lista principal: agrupada pelo vencimento e com a subtarefa recuada logo abaixo
    Então a tarefa "Configurar pipeline de CI/CD" aparece no grupo "Vencimento hoje"
    E a subtarefa "Criar workflow no GitHub Actions" aparece logo abaixo da tarefa "Configurar pipeline de CI/CD"
    # Nos itens do menu lateral: o filtro do dia e a lista do projeto
    E a tarefa "Configurar pipeline de CI/CD" aparece no filtro "Hoje"
    E a tarefa "Configurar pipeline de CI/CD" aparece na lista "Projetos de TI"
    # Ao reabrir: os dados continuam gravados
    E a tarefa "Configurar pipeline de CI/CD" mantém o vencimento "Hoje" e a prioridade alta ao ser reaberta
