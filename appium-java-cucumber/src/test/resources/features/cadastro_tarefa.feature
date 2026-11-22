# language: pt
@cadastro
Funcionalidade: Cadastro de tarefa
  Para acompanhar as demandas de cada projeto de T.I.
  Como pessoa que organiza o próprio trabalho
  Quero cadastrar tarefas com vencimento, prioridade, lista e subtarefas

  @smoke @severity=blocker
  Cenário: Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa
    Dado que existe a lista "Projetos de TI"
    Quando eu cadastro a tarefa "Configurar pipeline de CI/CD" com os dados:
      | vencimento | hoje                             |
      | prioridade | alta                             |
      | lista      | Projetos de TI                   |
      | subtarefa  | Criar workflow no GitHub Actions |
    Então a tarefa "Configurar pipeline de CI/CD" aparece no grupo "Vencimento hoje"
    E a subtarefa "Criar workflow no GitHub Actions" aparece logo abaixo da tarefa "Configurar pipeline de CI/CD"
    E a tarefa "Configurar pipeline de CI/CD" aparece no filtro "Hoje"
    E a tarefa "Configurar pipeline de CI/CD" aparece na lista "Projetos de TI"
    E a tarefa "Configurar pipeline de CI/CD" mantém o vencimento "Hoje" e a prioridade alta ao ser reaberta
