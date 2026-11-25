# appium-java-cucumber

> Parte do repositório [qa-mobile-automation-framework](../README.md). Para a versão com JUnit, veja [`appium-java`](../appium-java).

Framework de automação de testes Mobile em **BDD**, construído com **Java 21**, **Appium (UiAutomator2)**,
**Cucumber** e **JUnit Platform**, seguindo o padrão **Page Object Model**, com relatório no **Allure**.

O aplicativo testado é o [Tasks.org](https://github.com/tasks/tasks), um gerenciador de tarefas
Android open source. Os cenários são escritos em **Gherkin, em português**, e cobrem fluxos reais de
uso: cadastro completo de tarefas, edição, conclusão, exclusão, busca, ordenação e tarefas atrasadas.

## Sumário

- [Tecnologias](#tecnologias)
- [Funcionalidades e cenários](#funcionalidades-e-cenários)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configurando o aparelho](#configurando-o-aparelho)
- [Como rodar na sua máquina](#como-rodar-na-sua-máquina)
- [Opções de execução](#opções-de-execução)
- [Rodando pelo IntelliJ IDEA](#rodando-pelo-intellij-idea)
- [Relatório Allure](#relatório-allure)
- [Estado do aplicativo entre os cenários](#estado-do-aplicativo-entre-os-cenários)
- [Boas práticas adotadas](#boas-práticas-adotadas)
- [Como criar um novo cenário](#como-criar-um-novo-cenário)
- [Observações sobre o aplicativo](#observações-sobre-o-aplicativo)
- [Solução de problemas](#solução-de-problemas)
- [Licença](#licença)

## Tecnologias

| Ferramenta | Versão | Para que serve |
|---|---|---|
| Java (JDK) | 21 | Linguagem da automação |
| Appium Java Client | 11.0.0 | API do Appium para Java |
| Selenium | 4.53.0 | Base do Appium Java Client (esperas, localizadores) |
| Cucumber | 8.0.4 | Execução dos cenários Gherkin (BDD) |
| PicoContainer (Cucumber) | 8.0.4 | Compartilha o estado do cenário entre as classes de passos |
| JUnit Platform | 6.1.3 | Motor de execução usado pelo Maven e pelo IntelliJ |
| AssertJ | 3.27.7 | Validações fluentes e com mensagens claras |
| Allure (Java + Cucumber) | 3.1.1 | Registro dos passos, anexos e resultado de cada cenário |
| Allure Report | 2.30.0 | Relatório HTML com gráficos, ambiente da execução, passos e screenshots |
| Datafaker | 2.7.0 | Geração de dados aleatórios |
| Logback | 1.6.8 | Log da execução no terminal e nos anexos do Allure |
| Maven | 3.9+ | Build, dependências e execução pela linha de comando |
| Appium Server + UiAutomator2 | 3+ | Servidor que controla o aparelho Android |

## Funcionalidades e cenários

Os cenários ficam em `src/test/resources/features`. Cada cenário percorre **uma jornada de
negócio** (uma `Regra`) sobre a mesma massa: as tarefas são criadas uma única vez e cada etapa
`Quando`/`Então` parte do resultado da anterior, como no uso real do app. Cada cenário prepara e
limpa a própria massa, então pode ser executado sozinho e em qualquer ordem.

| Funcionalidade (tag) | Cenários | O que é validado |
|---|---|---|
| `cadastro_tarefa.feature` (`@cadastro`) | 1 (`@smoke`) | Cadastro completo com vencimento hoje, prioridade alta, lista "Projetos de TI" e subtarefa. Valida o grupo "Vencimento hoje", o recuo da subtarefa, o filtro "Hoje", a lista do projeto e os dados gravados ao reabrir |
| `ciclo_vida_tarefa.feature` (`@ciclo-vida`) | 2 | Edição: confirmação ao sair com alterações pendentes, continuar editando, descartar e salvar novo título. Conclusão: concluir com as concluídas ocultas e exibidas, reabrir e excluir (inclusive da busca) |
| `organizacao_lista.feature` (`@organizacao`) | 2 | Busca por termo existente e inexistente e ordenação por título e por prioridade sobre a mesma massa; tarefa vencida agrupada como "Atrasado" |

Exemplo:

```gherkin
# language: pt
Funcionalidade: Ciclo de vida da tarefa

  Contexto:
    Dado que existe a tarefa "Corrigir bug no login"

  Regra: Tarefas concluídas podem ser ocultadas, exibidas, reabertas e excluídas

    Cenário: Concluir, reabrir e excluir a tarefa
      Dado que as tarefas concluídas estão ocultas
      Quando eu concluo a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" deixa de aparecer na lista

      Quando eu passo a exibir as tarefas concluídas
      Então a tarefa "Corrigir bug no login" aparece no grupo "Concluída"

      Quando eu reabro a tarefa "Corrigir bug no login"
      Então a tarefa "Corrigir bug no login" aparece no grupo "Sem data de vencimento"
      ...
```

As validações comparam os textos do aplicativo em **português (Brasil)**.

## Estrutura do projeto

```
qa-mobile-automation-framework/
└── appium-java-cucumber/
    ├── pom.xml                                   # Dependências e plugins (Surefire e Allure)
    └── src
        ├── main/java
        │   ├── config/Configuracao.java          # Lê config.properties, config.local.properties e -D
        │   ├── driver/GerenciadorDriver.java     # Sessão Appium, capabilities, estado e data do aparelho
        │   └── pages/                            # Page objects: BasePage + uma classe por tela
        └── test
            ├── java
            │   ├── runner/ExecutarCenariosTest.java    # Suíte da JUnit Platform que aciona o Cucumber
            │   ├── steps/                        # Step definitions (Dado / Quando / Então)
            │   ├── hooks/Ganchos.java            # Hooks: sessão, tela inicial e limpeza da massa
            │   ├── hooks/TiposParametro.java     # Tipo {prioridade} usado nos passos
            │   ├── support/ContextoCenario.java  # Estado do cenário compartilhado entre os passos
            │   ├── support/Evidencias.java       # Screenshot e hierarquia da tela
            │   ├── support/AllureAmbiente.java   # Ambiente da execução na visão geral do Allure
            │   ├── support/CapturaLog.java       # Guarda os logs de cada passo e cenário para o Allure
            │   ├── support/LimpezaResultadosAllure.java  # Apaga os resultados da execução anterior
            │   └── support/RegistroEtapas.java   # Plugin: log no terminal e anexos (log, telas) no Allure
            └── resources
                ├── features/                     # Cenários em Gherkin (português)
                ├── config.properties             # Configuração padrão (versionada)
                ├── config.local.properties.exemplo  # Modelo dos dados do seu aparelho
                ├── junit-platform.properties     # Configuração do Cucumber (glue, plugins) para o Maven
                ├── cucumber.properties           # A mesma configuração, para rodar a feature pela IDE
                ├── allure.properties
                └── logback-test.xml              # Terminal (INFO) e anexos do Allure (DEBUG)
```

Como as peças se encaixam:

- **Features (`features/*.feature`)**: descrevem o comportamento em linguagem de negócio, sem
  detalhes de tela.
- **Passos (`steps/`)**: ligam cada frase do Gherkin aos Page Objects. São separados por assunto:
  cadastro, edição, conclusão, busca/ordenação e validações (`Então`).
- **`ContextoCenario`**: criado a cada cenário pelo PicoContainer e injetado nos passos. Guarda a
  tela atual e registra a massa criada, para a limpeza ao final.
- **`Ganchos`**: abre uma única sessão Appium para toda a execução (`@BeforeAll`), volta para
  "Minhas tarefas" antes de cada cenário (`@Before`) e, ao final do cenário (`@After`), exclui a
  massa.
- **`RegistroEtapas`** (plugin do Cucumber): escreve cada cenário e passo no terminal e anexa, dentro
  de cada passo do Allure, o log da etapa e o screenshot; na falha, também a hierarquia da tela. Fica
  antes do plugin do Allure em `cucumber.plugin`, para os anexos entrarem no passo certo.
- **Page objects (`pages/`)**: cada tela tem uma classe com seus localizadores e ações. Os passos
  só conversam com elas.
- **`BasePage`**: concentra as interações comuns (clicar, digitar, rolar, esperar), sempre com
  espera explícita.
- **`NavegacaoApp`**: leva o app de qualquer tela de volta para "Minhas tarefas".

## Pré-requisitos

| Programa | Versão | Observação |
|---|---|---|
| JDK | 21 | Precisa ser o **JDK** (com compilador), não só o JRE |
| Maven | 3.9 ou superior | Não é necessário se for rodar só pelo IntelliJ |
| Node.js | LTS | Necessário para instalar o Appium |
| Appium | 3 ou superior | `npm install -g appium` |
| Driver UiAutomator2 | atual | `appium driver install uiautomator2` |
| Android Platform-Tools | atual | Fornece o `adb`. A variável `ANDROID_HOME` deve apontar para a pasta do SDK |
| Aparelho Android | 8+ | Físico ou emulador, com o Tasks.org instalado |

### Variável ANDROID_HOME

O Appium precisa da variável `ANDROID_HOME` apontando para a pasta que contém o `platform-tools`.

No Windows (PowerShell):

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\caminho\do\android-sdk", "User")
```

No macOS/Linux (no `~/.zshrc` ou `~/.bashrc`):

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
```

Feche e abra o terminal para a variável valer.

### Conferindo a instalação

```bash
java -version                      # deve mostrar a versão 21
mvn -version                       # Maven 3.9+ com "Java version: 21"
appium -v                          # versão do Appium
appium driver list --installed     # deve listar o uiautomator2
adb devices                        # deve listar o aparelho como "device"
```

## Configurando o aparelho

1. Ative as **Opções do desenvolvedor** e a **Depuração USB** (ou depuração por Wi-Fi).
2. Instale o **Tasks.org** (Google Play, F-Droid ou APK do [GitHub](https://github.com/tasks/tasks/releases)).
3. Deixe o aparelho em **Português (Brasil)**.
4. Mantenha a tela desbloqueada durante a execução. Se quiser, ative **Permanecer ativo** nas
   Opções do desenvolvedor.
5. Informe os dados do aparelho **sem expô-los no repositório**:

   ```bash
   cd src/test/resources
   cp config.local.properties.exemplo config.local.properties
   ```

   Preencha o `config.local.properties` com o `udid` exibido pelo `adb devices` (e, se quiser, o
   modelo e a versão do Android). Esse arquivo é **ignorado pelo Git**. Com um único aparelho
   conectado, todas as chaves são opcionais: o Appium encontra o dispositivo sozinho.

## Como rodar na sua máquina

### 1. Clonar o repositório

```bash
git clone https://github.com/LucasQualityAssurance/qa-mobile-automation-framework.git
cd qa-mobile-automation-framework/appium-java-cucumber
```

> Todos os comandos `mvn` devem ser executados dentro da pasta `appium-java-cucumber`, onde fica o `pom.xml`.

### 2. Iniciar o servidor Appium (em um terminal separado)

```bash
appium --address 127.0.0.1 --port 4723
```

### 3. Rodar todos os cenários

```bash
mvn clean test
```

Durante a execução, cada cenário e cada passo aparecem no terminal:

```
INFO  [CENARIO] Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa
INFO  [ETAPA] Dado que existe a lista "Projetos de TI"
INFO  [ETAPA] Quando eu cadastro a tarefa "Configurar pipeline de CI/CD" com os dados:
...
5 scenarios (5 passed)
[INFO] BUILD SUCCESS
```

### 4. Rodar só uma parte dos cenários

```bash
# Somente o smoke (cadastro de tarefa)
mvn clean test -Psmoke

# Uma funcionalidade, pela tag
mvn clean test -Dcucumber.filter.tags="@ciclo-vida"

# Combinando tags
mvn clean test -Dcucumber.filter.tags="@organizacao and not @smoke"
```

### 5. Conferir os passos sem aparelho (dry-run)

Verifica se todas as frases das features têm um passo implementado, sem abrir sessão no Appium:

```bash
mvn clean test -Dcucumber.execution.dry-run=true
```

## Opções de execução

Todas as chaves do `config.properties` podem ser sobrescritas com `-D`
(ex.: `mvn test -Ddispositivo.udid=emulator-5554`).

| Opção | Padrão | O que faz |
|---|---|---|
| `-Dappium.url=<url>` | `http://127.0.0.1:4723` | Endereço do servidor Appium |
| `-Ddispositivo.udid=<udid>` | vazio | Aparelho usado na execução (obrigatório só com mais de um conectado) |
| `-Dapp.limpar.dados.inicio.suite=false` | `true` | Não limpa os dados do app no início da execução |
| `-Devidencia.sempre=false` | `true` | Deixa de anexar o screenshot de cada passo; as evidências de falha continuam |
| `-Dtempo.espera.padrao=<segundos>` | `15` | Tempo máximo das esperas explícitas |
| `-Psmoke` | — | Roda apenas os cenários marcados com `@smoke` |
| `-Dcucumber.filter.tags=<expressão>` | todos | Escolhe os cenários pelas tags |
| `-Dcucumber.execution.dry-run=true` | `false` | Só confere se todos os passos estão implementados |

## Rodando pelo IntelliJ IDEA

1. **File → Open** e selecione a pasta `appium-java-cucumber` (a que contém o `pom.xml`). Escolha abrir como
   projeto Maven e clique em **Trust Project**.
2. Confira o JDK em **File → Project Structure → Project → SDK**: deve ser um JDK 21.
3. Instale os plugins **Cucumber for Java** e **Gherkin** (Settings → Plugins) para navegar da
   feature até o passo com Ctrl+clique.
4. Com o servidor Appium rodando, abra `src/test/java/runner/ExecutarCenariosTest.java` e clique
   no ▶ verde ao lado da classe. Os cenários aparecem agrupados por funcionalidade.

Opções de execução vão em **Run → Edit Configurations… → VM options**
(ex.: `-Dcucumber.filter.tags=@smoke`).

Também é possível rodar uma feature ou um cenário pelo ▶ ao lado dele no arquivo `.feature`. Nesse
modo o IntelliJ chama o Cucumber pela linha de comando, que lê o `cucumber.properties` (e não o
`junit-platform.properties`); por isso os dois arquivos têm a mesma configuração. Uma diferença
em relação ao Maven:

- **Ações dos Page Objects como subpassos no Allure**: dependem do agente do AspectJ, que o Maven
  já configura. Para tê-las também pela IDE, adicione uma única vez em
  **Run → Edit Configurations… → Edit configuration templates… → Cucumber Java → VM options**:
  ```
  -javaagent:"<pasta do usuário>\.m2\repository\org\aspectj\aspectjweaver\1.9.27\aspectjweaver-1.9.27.jar"
  ```
  Sem o agente, o relatório continua com os passos, os logs e os screenshots.

## Relatório Allure

```bash
mvn clean test       # roda os cenários e grava os resultados em target/allure-results
mvn allure:serve     # gera o relatório e abre no navegador
```

Não é preciso instalar o Allure: o plugin do Maven baixa a ferramenta na primeira vez.

A cada execução, os resultados e as evidências (prints, logs, XML) da execução anterior são
apagados automaticamente, para o relatório mostrar só a execução atual e a pasta `target` não
crescer. Vale para o Maven e também para a execução pela IDE: a limpeza é feita pelo próprio
código, no início da execução (`support/LimpezaResultadosAllure.java`). O Maven também apaga o
relatório HTML gerado anteriormente. Para acumular várias execuções no mesmo relatório:

```bash
mvn test -Dallure.manter.resultados=true
```

O relatório mostra:

- a visão geral da execução, com os gráficos de resultado, severidade e duração e o ambiente
  (aparelho, versão do Android, aplicativo e servidor Appium);
- os cenários agrupados por funcionalidade, com as tags e a severidade (`@severity=blocker`,
  `@severity=critical`, `@severity=normal`);
- cada cenário com seus passos `Dado` / `Quando` / `Então`, as ações dos Page Objects dentro de
  cada passo e, como anexos do passo (abra o passo para vê-los):
  - **Tela após a etapa**: o screenshot ao final do passo;
  - **Log da etapa**: tudo o que aconteceu no passo (toques, textos digitados, esperas e
    localizadores usados, duração);
- em caso de falha, dentro do passo que falhou: o screenshot e a hierarquia da tela (XML) no
  momento do erro e o log com a mensagem de erro e o localizador que estourou o tempo;
- no corpo do cenário, visíveis sem abrir nenhum passo:
  - **Tela ao final do cenário** (ou **Tela no momento da falha**): o último print tirado;
  - **Log completo do cenário**: a preparação, todos os passos, a limpeza da massa e o resultado.

O terminal mostra só o essencial (nível INFO: cenários, passos e ações). Os anexos do Allure trazem
também o nível DEBUG (esperas, consultas e localizadores). Os níveis ficam no `logback-test.xml`.

Exemplo ilustrativo de "Log da etapa" (os localizadores reais variam conforme a tela):

```
07:12:03.114 INFO  [ETAPA] Quando eu altero o título da tarefa "Corrigir bug no login" para "Corrigir bug no login e publicar hotfix" e salvo
07:12:03.120 INFO  [TelaListaTarefas] Tocar: androidUIAutomator: new UiSelector().text("Corrigir bug no login")
07:12:03.121 DEBUG [TelaListaTarefas] Aguardando ficar clicável: androidUIAutomator: new UiSelector().text("Corrigir bug no login")
07:12:03.902 INFO  [TelaEdicaoTarefa] Digitar "Corrigir bug no login e publicar hotfix" em: id: org.tasks:id/title
07:12:04.517 DEBUG [ETAPA] Concluída: Quando eu altero o título da tarefa ... e salvo (1.4 s)
```

## Estado do aplicativo entre os cenários

- Os dados do app são limpos **uma única vez**, no início da execução, para partir de um estado
  conhecido (configurável em `app.limpar.dados.inicio.suite`).
- Entre os cenários o aplicativo **permanece aberto**: cada cenário apenas volta para
  "Minhas tarefas" pela própria interface.
- Cada cenário cria a própria massa (pelo `Contexto` ou pelos passos `Dado`) e o hook de limpeza
  exclui, ao final, todas as tarefas que ele criou, **mesmo quando o cenário falha**.
- Se o cenário ocultou as tarefas concluídas, a limpeza volta a exibi-las.
- A ordenação escolhida em `organizacao_lista.feature` continua gravada no app depois do cenário,
  como acontece para qualquer pessoa usuária. Os demais cenários não dependem dela.

## Boas práticas adotadas

- **BDD em linguagem de negócio**: as features não citam botões, ids ou telas; uma jornada por
  cenário, agrupada por `Regra`, com `Contexto` para a massa compartilhada.
- **Execução enxuta**: a massa é criada uma vez por jornada, em vez de recriada a cada
  comportamento, evitando repetir cadastro, navegação e limpeza a cada cenário.
- **Cenários independentes**: cada um prepara e limpa a própria massa e pode rodar sozinho.
- **Page Object Model**: localizadores e ações ficam nas telas; os passos descrevem o comportamento.
- **Esperas explícitas**: nenhuma sincronização com pausas fixas; o Appium aguarda o elemento ficar
  visível, clicável ou desaparecer.
- **Datas do aparelho**: "hoje" e "ontem" são calculados pelo relógio do aparelho, não do computador.
- **Localizadores estáveis**: preferência por `resource-id` e `testTag` do Compose; textos e XPath
  apenas onde o app não expõe identificadores.
- **Uma sessão por execução**: menos tempo de inicialização e o app sempre aberto.
- **Dados sensíveis fora do repositório**: os dados do aparelho ficam no `config.local.properties`.
- **Rastreabilidade**: cada passo aparece no terminal e no relatório, com evidência visual.

## Como criar um novo cenário

1. **Escreva o cenário** em uma feature de `src/test/resources/features` (ou crie uma nova, com
   `# language: pt` na primeira linha), reaproveitando as frases que já existem.
2. **Rode o dry-run** (`mvn test -Dcucumber.execution.dry-run=true`): o Cucumber lista as frases
   sem implementação e sugere o código do passo.
3. **Implemente os passos novos** na classe de `steps/` do assunto, recebendo o
   `ContextoCenario` no construtor. Registre as tarefas criadas com
   `contexto.registrarTarefaCriada(...)` para que a limpeza as exclua.
4. **Crie ou reaproveite os page objects** em `src/main/java/pages`, herdando de `BasePage`.
5. Rode com `mvn clean test -Dcucumber.filter.tags="@sua-tag"`.

## Observações sobre o aplicativo

Comportamentos da versão 15.14 do Tasks.org que influenciam a automação:

- Os `testTags` do Jetpack Compose (`save_task`, `due_date_row`...) não têm o prefixo do pacote; por
  isso a sessão ativa a configuração `disableIdLocatorAutocompletion` do UiAutomator2.
- "Mostrar tarefas concluídas" vem ativado por padrão, e o grupo "Concluída" fica recolhido.
- Os atalhos de título (`#tag`, `@tag`, `!prioridade`) não são interpretados pela tela de edição.
- Não há filtros prontos de "Prioridade alta" ou "Vencidas": a prioridade é validada na própria
  tarefa e as vencidas pelo agrupamento "Atrasado".
- Na busca, a tecla "voltar" pode fechar o aplicativo; a busca é fechada pelo botão "Recolher".

## Solução de problemas

| Problema | Causa provável | Como resolver |
|---|---|---|
| `Neither ANDROID_HOME nor ANDROID_SDK_ROOT environment variable was exported` | Variável não configurada no terminal do Appium | Configure o `ANDROID_HOME` e reinicie o terminal e o Appium |
| `Connection refused` em `127.0.0.1:4723` | Servidor Appium não está rodando | Inicie com `appium --address 127.0.0.1 --port 4723` |
| `Could not find a connected Android device` | Aparelho desconectado ou sem autorização de depuração | Confira com `adb devices` e aceite a autorização no aparelho |
| `Undefined step` / passo sem implementação | Frase da feature diferente da expressão do passo | Rode o dry-run e compare o texto; o Cucumber mostra a sugestão do passo |
| Teste parado na tela de bloqueio | Tela bloqueou durante a execução | Desbloqueie e ative **Permanecer ativo** ou aumente o tempo de tela |
| Elemento não encontrado por texto | Aparelho em outro idioma | Deixe o aparelho em Português (Brasil) |
| Mais de um aparelho conectado | O Appium não sabe qual usar | Informe o `dispositivo.udid` no `config.local.properties` ou via `-D` |
| `release version 21 not supported` | Maven rodando com Java mais antigo | Ajuste o `JAVA_HOME` para o JDK 21 |

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](../LICENSE).
