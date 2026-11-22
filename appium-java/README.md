# appium-java

> Parte do repositório [qa-mobile-automation-framework](../README.md). Para a versão BDD, veja [`appium-java-cucumber`](../appium-java-cucumber).

Framework de automação de testes Mobile construído com **Java 21**, **Appium (UiAutomator2)** e
**JUnit**, seguindo o padrão **Page Object Model**, com relatório no **Allure**.

O aplicativo testado é o [Tasks.org](https://github.com/tasks/tasks), um gerenciador de tarefas
Android open source. Os testes cobrem fluxos reais de uso: cadastro completo de tarefas, edição,
conclusão, exclusão, busca, ordenação e tarefas atrasadas.

## Sumário

- [Tecnologias](#tecnologias)
- [Fluxos de teste](#fluxos-de-teste)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configurando o aparelho](#configurando-o-aparelho)
- [Como rodar na sua máquina](#como-rodar-na-sua-máquina)
- [Opções de execução](#opções-de-execução)
- [Rodando pelo IntelliJ IDEA](#rodando-pelo-intellij-idea)
- [Relatório Allure](#relatório-allure)
- [Estado do aplicativo entre os testes](#estado-do-aplicativo-entre-os-testes)
- [Boas práticas adotadas](#boas-práticas-adotadas)
- [Como criar um novo teste](#como-criar-um-novo-teste)
- [Observações sobre o aplicativo](#observações-sobre-o-aplicativo)
- [Solução de problemas](#solução-de-problemas)
- [Licença](#licença)

## Tecnologias

| Ferramenta | Versão | Para que serve |
|---|---|---|
| Java (JDK) | 21 | Linguagem dos testes |
| Appium Java Client | 11.0.0 | API do Appium para Java |
| Selenium | 4.53.0 | Base do Appium Java Client (esperas, localizadores) |
| JUnit | 6.1.3 | Execução dos testes |
| AssertJ | 3.27.7 | Validações fluentes e com mensagens claras |
| Allure (Java) | 3.1.1 | Registro dos passos, anexos e resultado de cada teste |
| Allure Report | 2.30.0 | Relatório HTML com gráficos, ambiente da execução, passos e screenshots |
| Datafaker | 2.7.0 | Geração de dados aleatórios |
| Logback | 1.6.8 | Log da execução no terminal |
| Maven | 3.9+ | Build, dependências e execução pela linha de comando |
| Appium Server + UiAutomator2 | 3+ | Servidor que controla o aparelho Android |

## Fluxos de teste

Poucos testes, mas encorpados: cada um percorre um fluxo completo do aplicativo, dividido em
etapas que aparecem no terminal e no relatório Allure.

| Classe de teste | Fluxo | O que é validado |
|---|---|---|
| `CadastroTarefaTest` (`smoke`) | Cadastro de tarefa | Cria a lista "Projetos de TI" e a tarefa "Configurar pipeline de CI/CD" com vencimento hoje, prioridade alta e a subtarefa "Criar workflow no GitHub Actions". Valida na lista principal, no filtro "Hoje", na lista do projeto e ao reabrir a tarefa |
| `CicloVidaTarefaTest` | Ciclo de vida da tarefa | "Corrigir bug no login": continuar editando, descartar alterações, salvar novo título, concluir com e sem exibição das concluídas, reabrir e excluir (inclusive da busca) |
| `OrganizacaoListaTest` | Organização da lista | Tarefas de T.I. com prioridades diferentes: busca por termo existente e inexistente, ordenação por título e por prioridade e tarefa vencida agrupada como "Atrasado" |

Os nomes dos testes, das etapas e dos métodos estão em português. As validações comparam os textos
do aplicativo em **português (Brasil)**.

## Estrutura do projeto

```
qa-mobile-automation-framework/
└── appium-java/
    ├── pom.xml                                   # Dependências e plugins (Surefire e Allure)
    └── src
        ├── main/java
        │   ├── config/Configuracao.java          # Lê config.properties, config.local.properties e -D
        │   ├── driver/GerenciadorDriver.java     # Sessão Appium, capabilities e estado do app
        │   └── pages/                            # Page objects: BasePage + uma classe por tela
        └── test
            ├── java
            │   ├── base/TesteBase.java           # Prepara a tela inicial e registra as etapas
            │   ├── base/ExtensaoAppium.java      # Abre/fecha a sessão e anexa evidências
            │   ├── base/Evidencias.java          # Screenshot e hierarquia da tela no Allure
            │   ├── base/AllureAmbiente.java      # Ambiente da execução na visão geral do Allure
            │   └── testes/                       # Classes de teste
            └── resources
                ├── config.properties             # Configuração padrão (versionada)
                ├── config.local.properties.exemplo  # Modelo dos dados do seu aparelho
                ├── allure.properties
                ├── junit-platform.properties
                └── logback-test.xml
```

Como as peças se encaixam:

- **`TesteBase`**: toda classe de teste herda dele. Antes de cada teste, garante o app em primeiro
  plano e volta para a lista "Minhas tarefas", sem reiniciar o aplicativo. O método `etapa(...)`
  registra cada passo no terminal e no Allure, com screenshot.
- **`ExtensaoAppium`**: abre uma única sessão Appium para toda a execução, encerra no final e anexa
  screenshot e hierarquia da tela quando um teste falha.
- **Page objects (`pages/`)**: cada tela tem uma classe com seus localizadores e ações
  (`tocarEmNovaTarefa()`, `definirPrioridade()`, `salvar()`...). Os testes só conversam com elas.
- **`BasePage`**: concentra as interações comuns (clicar, digitar, rolar, esperar), sempre com
  espera explícita.
- **`NavegacaoApp`**: leva o app de qualquer tela de volta para "Minhas tarefas".
- **`GerenciadorDriver`**: cria a sessão com as capabilities do UiAutomator2 e controla o estado do app.

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
cd qa-mobile-automation-framework/appium-java
```

> Todos os comandos `mvn` devem ser executados dentro da pasta `appium-java`, onde fica o `pom.xml`.

### 2. Iniciar o servidor Appium (em um terminal separado)

```bash
appium --address 127.0.0.1 --port 4723
```

### 3. Rodar todos os testes

```bash
mvn clean test
```

Durante a execução, cada etapa aparece no terminal:

```
INFO  [ETAPA] 1. Validar a tela inicial "Minhas tarefas"
INFO  [ETAPA] 2. Criar a lista "Projetos de TI" pelo menu lateral
INFO  [ETAPA] 3. Abrir o formulário de nova tarefa
...
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 4. Rodar só uma parte dos testes

```bash
# Somente o smoke (cadastro de tarefa)
mvn clean test -Psmoke

# Uma classe
mvn clean test -Dtest=CicloVidaTarefaTest
```

## Opções de execução

Todas as chaves do `config.properties` podem ser sobrescritas com `-D`
(ex.: `mvn test -Ddispositivo.udid=emulator-5554`).

| Opção | Padrão | O que faz |
|---|---|---|
| `-Dappium.url=<url>` | `http://127.0.0.1:4723` | Endereço do servidor Appium |
| `-Ddispositivo.udid=<udid>` | vazio | Aparelho usado na execução (obrigatório só com mais de um conectado) |
| `-Dapp.limpar.dados.inicio.suite=false` | `true` | Não limpa os dados do app no início da execução |
| `-Devidencia.sempre=false` | `true` | Anexa screenshot ao final só dos testes que falharem |
| `-Dtempo.espera.padrao=<segundos>` | `15` | Tempo máximo das esperas explícitas |
| `-Psmoke` | — | Roda apenas os testes marcados com `@Tag("smoke")` |
| `-Dtest=<Classe>` | todos | Escolhe quais testes rodar |

## Rodando pelo IntelliJ IDEA

1. **File → Open** e selecione a pasta `appium-java` (a que contém o `pom.xml`). Escolha abrir como
   projeto Maven e clique em **Trust Project**.
2. Confira o JDK em **File → Project Structure → Project → SDK**: deve ser um JDK 21.
3. Com o servidor Appium rodando, abra uma classe em `src/test/java/testes` e clique no ▶ verde ao
   lado da classe. Para rodar tudo: botão direito na pasta `testes` → **Run 'Tests in 'testes''**.

Opções de execução vão em **Run → Edit Configurations… → VM options**.

## Relatório Allure

```bash
mvn clean test       # roda os testes e grava os resultados em target/allure-results
mvn allure:serve     # gera o relatório e abre no navegador
```

Não é preciso instalar o Allure: o plugin do Maven baixa a ferramenta na primeira vez.

A cada execução, os resultados, as evidências e o relatório da execução anterior são apagados
automaticamente, para o relatório mostrar só a execução atual e a pasta `target` não crescer.
Para acumular várias execuções no mesmo relatório:

```bash
mvn test -Dallure.manter.resultados=true
```

O relatório mostra:

- a visão geral da execução, com os gráficos de resultado, severidade e duração e o ambiente
  (aparelho, versão do Android, aplicativo e servidor Appium);
- os testes agrupados por suíte e por funcionalidade, além da linha do tempo da execução;
- cada fluxo com suas etapas numeradas, em ordem;
- o screenshot da tela ao final de cada etapa;
- em caso de falha, o screenshot e a hierarquia da tela (XML) no momento do erro;
- a descrição do fluxo, a funcionalidade (`@Feature`) e a severidade de cada teste.

## Estado do aplicativo entre os testes

- Os dados do app são limpos **uma única vez**, no início da execução, para partir de um estado
  conhecido (configurável em `app.limpar.dados.inicio.suite`).
- Entre os testes o aplicativo **permanece aberto**: cada teste apenas volta para "Minhas tarefas"
  pela própria interface.
- Cada fluxo exclui ao final as tarefas que criou e valida somente a própria massa de dados, então
  os testes não interferem uns nos outros.

## Boas práticas adotadas

- **Page Object Model**: localizadores e ações ficam nas telas; os testes descrevem o comportamento.
- **Esperas explícitas**: nenhuma sincronização com pausas fixas; o Appium aguarda o elemento ficar
  visível, clicável ou desaparecer.
- **Localizadores estáveis**: preferência por `resource-id` e `testTag` do Compose; textos e XPath
  apenas onde o app não expõe identificadores.
- **Uma sessão por execução**: menos tempo de inicialização e o app sempre aberto.
- **Dados sensíveis fora do repositório**: os dados do aparelho ficam no `config.local.properties`.
- **Rastreabilidade**: cada etapa aparece no terminal e no relatório, com evidência visual.

## Como criar um novo teste

1. **Crie ou reaproveite os page objects** em `src/main/java/pages`, herdando de `BasePage`.
2. **Crie a classe de teste** em `src/test/java/testes`, herdando de `TesteBase`, com `@Test`,
   `@DisplayName`, `@Feature` e `@Description`.
3. **Escreva o fluxo em etapas**: `etapa("1. Descrição", () -> { ação; validação; });`.
4. **Limpe a massa** criada pelo teste na última etapa.
5. Rode com `mvn clean test -Dtest=NomeDaClasseTest`.

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
| Teste parado na tela de bloqueio | Tela bloqueou durante a execução | Desbloqueie e ative **Permanecer ativo** ou aumente o tempo de tela |
| Elemento não encontrado por texto | Aparelho em outro idioma | Deixe o aparelho em Português (Brasil) |
| Mais de um aparelho conectado | O Appium não sabe qual usar | Informe o `dispositivo.udid` no `config.local.properties` ou via `-D` |
| `release version 21 not supported` | Maven rodando com Java mais antigo | Ajuste o `JAVA_HOME` para o JDK 21 |

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](../LICENSE).
