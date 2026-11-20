# qa-mobile-automation-framework

![Java](https://img.shields.io/badge/Java-21-orange)
![Appium](https://img.shields.io/badge/Appium-UiAutomator2-662d91)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Allure](https://img.shields.io/badge/Allure-Report-yellow)
![License](https://img.shields.io/badge/license-MIT-blue)

Portfólio de automação de testes **Mobile** em **Java + Appium**, com duas abordagens para o mesmo
aplicativo Android: testes escritos **100% em Java** com JUnit e testes **BDD com Cucumber**. Os
projetos cobrem os principais fluxos do app e geram relatório no **Allure**.

## Aplicativo testado

Os testes automatizam o [**Tasks.org**](https://github.com/tasks/tasks), um gerenciador de tarefas
Android **open source**. Ele reúne situações comuns do dia a dia de QA mobile: formulários,
seletores de data, diálogos de confirmação, menu lateral, busca, ordenação, listas agrupadas e
telas construídas com Jetpack Compose e com Views tradicionais.

## Projetos

| Pasta | Abordagem | Status |
|---|---|---|
| [`appium-java/`](appium-java) | Java puro: JUnit + Page Object Model | ✅ Pronto |
| [`appium-java-cucumber/`](appium-java-cucumber) | BDD: Cucumber + Gherkin em português + Page Object Model | 🚧 Em construção |

Cada pasta é um projeto Maven independente, com o próprio `pom.xml` e o próprio README explicando
como rodar.

### appium-java: Java 100%

Testes escritos em Java com **JUnit**, seguindo o padrão **Page Object Model**: cada tela do app
tem uma classe com seus localizadores e ações, e os testes só conversam com essas classes.

- 3 fluxos ponta a ponta e detalhados: cadastro completo de tarefa, ciclo de vida da tarefa
  (editar, concluir, reabrir e excluir) e organização da lista (busca, ordenação e atrasadas).
- Cada etapa do fluxo vira um passo no relatório Allure, com screenshot próprio.
- Uma única sessão Appium por execução, com o app aberto do início ao fim.
- Dados do aparelho fora do repositório (`config.local.properties`, ignorado pelo Git).

👉 Detalhes, fluxos e passo a passo em [`appium-java/README.md`](appium-java/README.md).

### appium-java-cucumber: BDD

Os mesmos fluxos escritos como cenários em **Gherkin** (`Dado / Quando / Então`), em português,
ligados a step definitions em Java e executados pelo **Cucumber**. Em construção.

## Estrutura do repositório

```
qa-mobile-automation-framework/
├── appium-java/             # Projeto JUnit + Page Object Model
├── appium-java-cucumber/    # Projeto BDD com Cucumber (em construção)
├── LICENSE
└── README.md
```

## Tecnologias

| Ferramenta | Uso |
|---|---|
| Java 21 | Linguagem dos testes |
| Appium + UiAutomator2 | Controle do aplicativo Android |
| Appium Java Client / Selenium 4 | API de automação usada pelo código |
| JUnit | Execução dos testes do `appium-java` |
| Cucumber | Cenários BDD do `appium-java-cucumber` |
| AssertJ | Validações legíveis e com mensagens claras |
| Maven | Build, dependências e execução pela linha de comando |
| Allure Report | Relatório com os passos e screenshots de cada teste |

## Como rodar localmente

Pré-requisitos: **JDK 21**, **Maven 3.9+**, **Node.js**, **Appium** com o driver
**UiAutomator2**, **Android Platform-Tools** e um aparelho Android (físico ou emulador) com o
Tasks.org instalado.

```bash
git clone https://github.com/LucasQualityAssurance/qa-mobile-automation-framework.git
cd qa-mobile-automation-framework/appium-java

# Em outro terminal: appium --address 127.0.0.1 --port 4723
mvn clean test
mvn allure:serve
```

O passo a passo completo (instalação, configuração do aparelho, IntelliJ e solução de problemas)
está no README de cada projeto.

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
