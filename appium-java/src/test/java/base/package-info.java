/**
 * Infraestrutura comum a todos os testes.
 * <ul>
 *   <li>{@link base.TesteBase}: classe que todo teste estende. Leva o app à lista
 *       "Minhas tarefas" antes de cada teste e oferece o método {@code etapa(...)}.</li>
 *   <li>{@link base.ExtensaoAppium}: extensão do JUnit que abre a sessão Appium uma única vez e
 *       anexa evidências ao relatório quando um teste falha.</li>
 *   <li>{@link base.Evidencias}: captura de screenshot e da hierarquia da tela (XML) para o Allure.</li>
 *   <li>{@code AllureAmbiente}: grava aparelho, versão do Android e afins na visão geral do relatório.</li>
 * </ul>
 * Ordem do que acontece numa execução:
 * <pre>
 *  1. ExtensaoAppium.beforeAll      → abre a sessão Appium (só no primeiro teste)
 *                                     e limpa os dados do app, se configurado
 *  2. TesteBase.prepararTelaInicial → leva o app até "Minhas tarefas"
 *  3. método @Test                  → executa as etapas do fluxo
 *  4. ExtensaoAppium                → anexa screenshot (e XML, em caso de falha)
 *  5. repete 2 a 4 para os demais testes
 *  6. fim da execução               → fecha a sessão Appium
 * </pre>
 */
package base;
