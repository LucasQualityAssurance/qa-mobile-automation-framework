/**
 * Testes ponta a ponta do aplicativo Tasks.org. Cada classe é um fluxo longo, dividido em etapas
 * numeradas que aparecem no terminal e no relatório Allure.
 * <ul>
 *   <li>{@code CadastroTarefaTest}: cria uma tarefa completa e confere onde ela aparece.</li>
 *   <li>{@code CicloVidaTarefaTest}: edita, descarta, conclui, reabre e exclui uma tarefa.</li>
 *   <li>{@code OrganizacaoListaTest}: busca, ordenação e agrupamento de tarefas atrasadas.</li>
 * </ul>
 *
 * <h2>Como criar um novo teste</h2>
 * <ol>
 *   <li>Crie uma classe {@code NomeDoFluxoTest} neste pacote estendendo {@link base.TesteBase}.</li>
 *   <li>Anote com {@code @Feature} e {@code @DisplayName} (aparecem no relatório).</li>
 *   <li>No método {@code @Test}, comece pela variável {@code lista}, que já está na tela
 *       "Minhas tarefas", e escreva cada passo dentro de {@code etapa("N. descrição", () -> ...)}.</li>
 *   <li>Use apenas métodos dos Page Objects (pacote {@code pages}); nada de localizadores aqui.</li>
 *   <li>Ao final, exclua as tarefas criadas, para não interferir nos próximos testes.</li>
 * </ol>
 * As classes rodam em ordem alfabética e em sequência (ver {@code junit-platform.properties}).
 */
package testes;
