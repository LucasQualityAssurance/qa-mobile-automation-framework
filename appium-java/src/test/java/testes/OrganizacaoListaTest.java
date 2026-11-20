package testes;

import base.TesteBase;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.PainelOrdenacao;
import pages.Prioridade;

import java.text.Collator;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Organização da lista")
@DisplayName("Organização da lista")
class OrganizacaoListaTest extends TesteBase {

    private static final String REVISAR_PR = "Revisar pull request da API";
    private static final String APLICAR_PATCH = "Aplicar patch de segurança";
    private static final String REVISAR_DOC = "Revisar documentação técnica";
    private static final String CERTIFICADO = "Renovar certificado SSL";
    private static final List<String> TAREFAS_DO_TESTE = List.of(REVISAR_PR, APLICAR_PATCH, REVISAR_DOC);

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar, ordenar e identificar tarefas atrasadas")
    @Description("""
            Com tarefas de prioridades diferentes cadastradas, valida a busca por termo
            existente e inexistente, a ordenação por título e por prioridade e o agrupamento
            das tarefas vencidas como "Atrasado". Ao final, remove a massa criada.""")
    void organizarListaDeTarefas() {
        etapa("Pré-condição: cadastrar tarefas com prioridades diferentes", () -> {
            lista.tocarEmNovaTarefa().informarTitulo(REVISAR_PR).definirPrioridade(Prioridade.BAIXA).salvar();
            lista.tocarEmNovaTarefa().informarTitulo(APLICAR_PATCH).definirPrioridade(Prioridade.ALTA).salvar();
            lista.tocarEmNovaTarefa().informarTitulo(REVISAR_DOC).definirPrioridade(Prioridade.MEDIA).salvar();
            assertThat(lista.obterTitulosDasTarefas()).containsAll(TAREFAS_DO_TESTE);
        });

        etapa("1. Buscar por \"Revisar\": somente as tarefas correspondentes", () -> {
            lista.buscar("Revisar");
            assertThat(lista.obterTitulosDasTarefas()).containsExactlyInAnyOrder(REVISAR_PR, REVISAR_DOC);
            lista.fecharBusca();
        });

        etapa("2. Buscar por termo inexistente: lista vazia", () -> {
            lista.buscar(new Faker().regexify("[a-z]{12}"));
            assertThat(lista.estaVazia()).as("Mensagem 'Não há tarefas aqui.'").isTrue();
            lista.fecharBusca();
        });

        etapa("3. Ordenar por título: ordem alfabética", () -> {
            lista.abrirOrdenacao().ordenarPor(PainelOrdenacao.ORDEM_POR_TITULO);
            assertThat(tarefasDoTesteNaOrdemExibida())
                    .containsExactly(APLICAR_PATCH, REVISAR_DOC, REVISAR_PR)
                    .isSortedAccordingTo(Collator.getInstance(Locale.of("pt", "BR")));
        });

        etapa("4. Ordenar por prioridade: prioridade alta no topo", () -> {
            lista.abrirOrdenacao().ordenarPor(PainelOrdenacao.ORDEM_POR_PRIORIDADE);
            assertThat(tarefasDoTesteNaOrdemExibida()).containsExactly(APLICAR_PATCH, REVISAR_DOC, REVISAR_PR);
        });

        etapa("5. Cadastrar tarefa com vencimento ontem: agrupada como \"Atrasado\"", () -> {
            lista.tocarEmNovaTarefa()
                    .informarTitulo(CERTIFICADO)
                    .definirVencimento(LocalDate.now().minusDays(1))
                    .salvar();
            assertThat(lista.obterGrupoDaTarefa(CERTIFICADO)).hasValue("Atrasado");
        });

        etapa("6. Excluir as tarefas criadas (limpeza da massa)", () -> {
            for (String tarefa : List.of(REVISAR_PR, APLICAR_PATCH, REVISAR_DOC, CERTIFICADO)) {
                lista = lista.abrirTarefa(tarefa).excluir();
            }
            assertThat(lista.obterTitulosDasTarefas()).doesNotContainAnyElementsOf(TAREFAS_DO_TESTE);
        });
    }

    /** Considera somente as tarefas deste teste, preservando a ordem exibida na tela. */
    private List<String> tarefasDoTesteNaOrdemExibida() {
        return lista.obterTitulosDasTarefas().stream().filter(TAREFAS_DO_TESTE::contains).toList();
    }
}
