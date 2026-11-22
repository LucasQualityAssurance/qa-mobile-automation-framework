/**
 * Page Objects: uma classe para cada tela (ou parte de tela) do aplicativo Tasks.org.
 *
 * <h2>Como ler este pacote</h2>
 * Cada classe representa o que o usuário vê e pode fazer em uma tela. Os testes nunca usam
 * localizadores diretamente: eles chamam os métodos destas classes, como
 * {@code lista.tocarEmNovaTarefa().informarTitulo("...").salvar()}.
 *
 * <h2>Mapa das telas</h2>
 * <pre>
 *  TelaBoasVindas ──(continuar sem sincronizar)──► TelaListaTarefas  (tela principal)
 *                                                    │
 *        ┌───────────────────────┬───────────────────┼──────────────────────┐
 *        ▼                       ▼                   ▼                      ▼
 *   MenuLateral          TelaEdicaoTarefa      PainelOrdenacao        busca (na própria
 *   (filtros e listas)   (criar/editar)        (ordenar, mostrar      TelaListaTarefas)
 *        │                │        │            concluídas)
 *        ▼                ▼        ▼
 *   TelaNovaLista     SeletorData  SeletorLista
 *   (criar lista)     (vencimento) (lista da tarefa)
 * </pre>
 *
 * <h2>Convenções</h2>
 * <ul>
 *   <li>Toda classe estende {@link pages.BasePage}, que concentra as esperas e ações comuns.</li>
 *   <li>Métodos que levam a outra tela devolvem o Page Object dessa tela já carregada,
 *       permitindo encadear chamadas.</li>
 *   <li>{@code aguardarCarregamento()} / {@code aguardarAbertura()} esperam o elemento que prova
 *       que a tela está pronta para uso.</li>
 *   <li>Métodos com {@code @Step} aparecem como passos no relatório Allure.</li>
 *   <li>Localizadores com o prefixo {@code org.tasks:id/} são de telas em XML (Android Views);
 *       os sem prefixo, como {@code save_task}, são {@code testTag} de telas em Jetpack Compose.</li>
 * </ul>
 *
 * <p>{@link pages.NavegacaoApp} não é uma tela: ela leva o app de qualquer tela de volta à
 * lista "Minhas tarefas" e é usada antes de cada teste.
 */
package pages;
