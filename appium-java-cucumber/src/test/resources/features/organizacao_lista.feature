# language: pt
# =====================================================================================
# COMO LER ESTE ARQUIVO (para quem está chegando ao projeto)
#
# - Este arquivo tem duas "Regra"s. O "Contexto" que está DENTRO da primeira Regra só
#   roda antes dos cenários dessa Regra (cria as três tarefas da busca e da ordenação).
# - Tabelas com cabeçalho (| título | prioridade |) viram uma lista de linhas no passo:
#   para incluir mais uma tarefa na massa, basta acrescentar uma linha.
# - O código de cada passo fica em src/test/java/steps (Ctrl+clique na frase, no IntelliJ).
# - Executar só esta funcionalidade: mvn test -Dcucumber.filter.tags="@organizacao"
# - No Allure, cada passo traz o print da tela e o log do que foi feito.
# =====================================================================================
@organizacao
Funcionalidade: Organização da lista de tarefas
  Para encontrar rapidamente o que precisa ser feito
  Como pessoa que organiza o próprio trabalho
  Quero buscar, ordenar e identificar as tarefas atrasadas

  Regra: A busca filtra pelo título e a ordenação escolhida reorganiza a lista

    # Cada tarefa tem uma prioridade diferente e os títulos não seguem a ordem das prioridades:
    # assim a ordenação por título e a ordenação por prioridade produzem resultados distintos.
    # Duas tarefas começam com "Revisar", para a busca encontrar só uma parte da massa.
    # O Contexto fica dentro da Regra: o cenário de tarefa atrasada, mais abaixo, não precisa
    # destas três tarefas e não paga o tempo de criá-las.
    Contexto:
      Dado que existem as tarefas:
        | título                       | prioridade |
        | Revisar pull request da API  | alta       |
        | Aplicar patch de segurança   | baixa      |
        | Revisar documentação técnica | média      |

    @severity=normal
    Cenário: Buscar e ordenar as tarefas cadastradas
      # 1. Busca por termo existente: só as tarefas que contêm "Revisar" (o passo Então fecha a busca)
      Quando eu busco por "Revisar"
      Então o resultado da busca contém somente as tarefas:
        | Revisar pull request da API  |
        | Revisar documentação técnica |

      # 2. Busca por termo inexistente (sorteado a cada execução): nenhuma tarefa
      Quando eu busco por um termo inexistente
      Então a busca não encontra tarefas

      # 3. Por título: ordem alfabética
      Quando eu ordeno a lista "Por título"
      Então as tarefas aparecem na ordem "Aplicar patch de segurança", "Revisar documentação técnica" e "Revisar pull request da API"

      # 4. Por prioridade: alta, média e baixa
      # (a ordem considera só estas três tarefas; outras que existam no aparelho são ignoradas)
      Quando eu ordeno a lista "Por prioridade"
      Então as tarefas aparecem na ordem "Revisar pull request da API", "Revisar documentação técnica" e "Aplicar patch de segurança"

  Regra: Tarefas com vencimento no passado ficam agrupadas como atrasadas

    # "ontem" é calculado pela data do aparelho, não do computador.
    @severity=normal
    Cenário: Tarefa vencida aparece no grupo "Atrasado"
      Quando eu cadastro a tarefa "Renovar certificado SSL" com os dados:
        | vencimento | ontem |
      Então a tarefa "Renovar certificado SSL" aparece no grupo "Atrasado"
