package testes;

import base.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.Prioridade;
import pages.TelaEdicaoTarefa;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cadastro de tarefa")
class CadastroTarefaTest extends TesteBase {

    private static final String TAREFA = "Configurar pipeline de CI/CD";
    private static final String SUBTAREFA = "Criar workflow no GitHub Actions";
    private static final String LISTA_PROJETO = "Projetos de TI";

    private TelaEdicaoTarefa edicao;

    @Test
    @Tag("smoke")
    @DisplayName("Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa")
    void cadastrarTarefaCompleta() {
        etapa("1. Validar a tela inicial \"Minhas tarefas\"", () ->
                assertThat(lista.obterTituloDaTela()).isEqualTo("Minhas tarefas"));

        etapa("2. Criar a lista \"" + LISTA_PROJETO + "\" pelo menu lateral", () -> {
            lista.abrirMenuLateral().criarLista(LISTA_PROJETO);
            lista = lista.abrirMenuLateral().selecionar("Minhas tarefas");
            assertThat(lista.obterTituloDaTela()).isEqualTo("Minhas tarefas");
        });

        etapa("3. Abrir o formulário de nova tarefa", () -> {
            edicao = lista.tocarEmNovaTarefa();
            assertThat(edicao.estaAberta()).isTrue();
        });

        etapa("4. Informar o título \"" + TAREFA + "\"", () -> {
            edicao.informarTitulo(TAREFA);
            assertThat(edicao.obterTitulo()).isEqualTo(TAREFA);
        });

        etapa("5. Definir o vencimento para hoje", () -> {
            edicao.definirVencimentoHoje();
            assertThat(edicao.obterTextoVencimento()).isEqualTo("Hoje");
        });

        etapa("6. Definir a prioridade alta", () -> {
            edicao.definirPrioridade(Prioridade.ALTA);
            assertThat(edicao.obterPrioridadeSelecionada()).isEqualTo(Prioridade.ALTA);
        });

        etapa("7. Associar a tarefa à lista \"" + LISTA_PROJETO + "\"", () ->
                edicao.selecionarLista(LISTA_PROJETO));

        etapa("8. Adicionar a subtarefa \"" + SUBTAREFA + "\"", () ->
                edicao.adicionarSubtarefa(SUBTAREFA));

        etapa("9. Salvar a tarefa", () -> {
            lista = edicao.salvar();
            assertThat(lista.obterTituloDaTela()).isEqualTo("Minhas tarefas");
        });

        etapa("10. Validar a tarefa e a subtarefa na lista principal", () -> {
            assertThat(lista.obterTitulosDasTarefas()).containsSubsequence(TAREFA, SUBTAREFA);
            assertThat(lista.obterGrupoDaTarefa(TAREFA)).hasValue("Vencimento hoje");
            assertThat(lista.obterRecuoDaTarefa(SUBTAREFA))
                    .as("Subtarefa exibida com recuo sob a tarefa pai")
                    .isGreaterThan(lista.obterRecuoDaTarefa(TAREFA));
        });

        etapa("11. Validar a tarefa no filtro \"Hoje\"", () -> {
            lista = lista.abrirMenuLateral().selecionar("Hoje");
            assertThat(lista.obterTituloDaTela()).isEqualTo("Hoje");
            assertThat(lista.exibeTarefa(TAREFA)).isTrue();
        });

        etapa("12. Validar a tarefa na lista \"" + LISTA_PROJETO + "\"", () -> {
            lista = lista.abrirMenuLateral().selecionar(LISTA_PROJETO);
            assertThat(lista.obterTituloDaTela()).isEqualTo(LISTA_PROJETO);
            assertThat(lista.exibeTarefa(TAREFA)).isTrue();
        });

        etapa("13. Reabrir a tarefa e validar os dados gravados", () -> {
            edicao = lista.abrirTarefa(TAREFA);
            assertThat(edicao.obterTitulo()).isEqualTo(TAREFA);
            assertThat(edicao.obterTextoVencimento()).isEqualTo("Hoje");
            assertThat(edicao.obterPrioridadeSelecionada()).isEqualTo(Prioridade.ALTA);
        });

        etapa("14. Excluir a tarefa criada (limpeza da massa)", () -> {
            lista = edicao.excluir();
            assertThat(lista.obterTitulosDasTarefas()).doesNotContain(TAREFA, SUBTAREFA);
        });
    }
}
