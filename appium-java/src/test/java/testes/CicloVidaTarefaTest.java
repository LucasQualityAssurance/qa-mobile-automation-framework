package testes;

import base.TesteBase;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.TelaEdicaoTarefa;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Ciclo de vida da tarefa")
@DisplayName("Ciclo de vida da tarefa")
class CicloVidaTarefaTest extends TesteBase {

    private static final String TITULO_ORIGINAL = "Corrigir bug no login";
    private static final String TITULO_RASCUNHO = "Corrigir bug no login do app";
    private static final String TITULO_FINAL = "Corrigir bug no login e publicar hotfix";
    private static final String GRUPO_CONCLUIDAS = "Concluída";

    private TelaEdicaoTarefa edicao;

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Editar, concluir, reabrir e excluir uma tarefa")
    @Description("""
            Percorre o ciclo de vida completo de uma tarefa: edição com as opções de
            continuar editando e descartar, gravação do novo título, conclusão (com e sem
            exibição das concluídas), reabertura e exclusão definitiva.""")
    void percorrerCicloDeVidaDaTarefa() {
        etapa("Pré-condição: cadastrar a tarefa \"" + TITULO_ORIGINAL + "\"", () -> {
            lista.criarTarefa(TITULO_ORIGINAL);
            assertThat(lista.exibeTarefa(TITULO_ORIGINAL)).isTrue();
        });

        etapa("1. Alterar o título, voltar e escolher \"Continuar editando\"", () -> {
            edicao = lista.abrirTarefa(TITULO_ORIGINAL).informarTitulo(TITULO_RASCUNHO).voltarDaEdicao();
            assertThat(edicao.exibeDialogoDescarte()).as("Diálogo de descarte exibido").isTrue();
            edicao.continuarEditando();
            assertThat(edicao.obterTitulo()).as("Alteração mantida na edição").isEqualTo(TITULO_RASCUNHO);
        });

        etapa("2. Voltar novamente e descartar as alterações", () -> {
            lista = edicao.voltarDaEdicao().descartarAlteracoes();
            assertThat(lista.exibeTarefa(TITULO_ORIGINAL)).as("Título original mantido").isTrue();
            assertThat(lista.obterTitulosDasTarefas()).doesNotContain(TITULO_RASCUNHO);
        });

        etapa("3. Editar e salvar o novo título \"" + TITULO_FINAL + "\"", () -> {
            lista = lista.abrirTarefa(TITULO_ORIGINAL).informarTitulo(TITULO_FINAL).salvar();
            assertThat(lista.obterTitulosDasTarefas()).contains(TITULO_FINAL).doesNotContain(TITULO_ORIGINAL);
        });

        etapa("4. Ocultar as concluídas e concluir a tarefa: ela sai da lista", () -> {
            lista.abrirOrdenacao().definirMostrarConcluidas(false).fechar();
            lista.alternarConclusao(TITULO_FINAL);
            lista.aguardarTarefaSumir(TITULO_FINAL);
            assertThat(lista.obterTitulosDasTarefas()).doesNotContain(TITULO_FINAL);
        });

        etapa("5. Exibir as concluídas: a tarefa aparece no grupo \"" + GRUPO_CONCLUIDAS + "\"", () -> {
            lista.abrirOrdenacao().definirMostrarConcluidas(true).fechar();
            lista.expandirGrupo(GRUPO_CONCLUIDAS);
            assertThat(lista.obterGrupoDaTarefa(TITULO_FINAL)).hasValue(GRUPO_CONCLUIDAS);
        });

        etapa("6. Reabrir a tarefa: ela volta a ficar ativa", () -> {
            lista.alternarConclusao(TITULO_FINAL);
            assertThat(lista.obterGrupoDaTarefa(TITULO_FINAL)).hasValue("Sem data de vencimento");
        });

        etapa("7. Excluir a tarefa: ela não aparece na lista nem na busca", () -> {
            lista = lista.abrirTarefa(TITULO_FINAL).excluir();
            assertThat(lista.obterTitulosDasTarefas()).doesNotContain(TITULO_FINAL);
            lista.buscar(TITULO_FINAL);
            assertThat(lista.estaVazia()).as("Busca sem resultados").isTrue();
            lista.fecharBusca();
        });
    }
}
