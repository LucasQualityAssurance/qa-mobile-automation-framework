# language: pt
@organizacao
Funcionalidade: Organização da lista de tarefas
  Para encontrar rapidamente o que precisa ser feito
  Como pessoa que organiza o próprio trabalho
  Quero buscar, ordenar e identificar as tarefas atrasadas

  # Cada tarefa tem uma prioridade diferente e os títulos não seguem a ordem das prioridades:
  # assim a ordenação por título e a ordenação por prioridade produzem resultados distintos.
  Contexto:
    Dado que existem as tarefas:
      | título                       | prioridade |
      | Revisar pull request da API  | alta       |
      | Aplicar patch de segurança   | baixa      |
      | Revisar documentação técnica | média      |

  Regra: A busca exibe somente as tarefas cujo título contém o termo pesquisado

    @severity=normal
    Cenário: Buscar por um termo presente em algumas tarefas
      Quando eu busco por "Revisar"
      Então o resultado da busca contém somente as tarefas:
        | Revisar pull request da API  |
        | Revisar documentação técnica |

    @severity=normal
    Cenário: Buscar por um termo que não existe em nenhuma tarefa
      Quando eu busco por um termo inexistente
      Então a busca não encontra tarefas

  Regra: A ordenação escolhida reorganiza a lista

    @severity=normal
    Esquema do Cenário: Ordenar a lista "<ordem>"
      Quando eu ordeno a lista "<ordem>"
      Então as tarefas aparecem na ordem "<primeira>", "<segunda>" e "<terceira>"

      Exemplos:
        | ordem          | primeira                    | segunda                      | terceira                    |
        | Por título     | Aplicar patch de segurança  | Revisar documentação técnica | Revisar pull request da API |
        | Por prioridade | Revisar pull request da API | Revisar documentação técnica | Aplicar patch de segurança  |

  Regra: Tarefas com vencimento no passado ficam agrupadas como atrasadas

    @severity=normal
    Cenário: Tarefa vencida aparece no grupo "Atrasado"
      Quando eu cadastro a tarefa "Renovar certificado SSL" com os dados:
        | vencimento | ontem |
      Então a tarefa "Renovar certificado SSL" aparece no grupo "Atrasado"
