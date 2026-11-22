package passos;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import pages.TelaListaTarefas;
import suporte.ContextoCenario;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos de conclusão e reabertura de tarefas e da preferência "Mostrar tarefas concluídas".
 * <p>
 * A preferência fica gravada no aplicativo. Quando um cenário oculta as concluídas, isso é
 * registrado no {@link ContextoCenario} e o hook de limpeza volta a exibi-las ao final.
 */
public class PassosConclusao {

    private static final String GRUPO_CONCLUIDAS = "Concluída";

    private final ContextoCenario contexto;

    public PassosConclusao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que as tarefas concluídas estão ocultas")
    public void queAsTarefasConcluidasEstaoOcultas() {
        contexto.registrarExibicaoConcluidasAlterada();
        contexto.definirLista(contexto.lista().abrirOrdenacao().definirMostrarConcluidas(false).fechar());
    }

    @Dado("que as tarefas concluídas estão sendo exibidas")
    public void queAsTarefasConcluidasEstaoSendoExibidas() {
        contexto.definirLista(contexto.lista().abrirOrdenacao().definirMostrarConcluidas(true).fechar());
    }

    @Dado("que a tarefa {string} está concluída")
    public void queATarefaEstaConcluida(String titulo) {
        queAsTarefasConcluidasEstaoSendoExibidas();
        TelaListaTarefas lista = contexto.lista().alternarConclusao(titulo).localizarTarefaNoGrupo(titulo, GRUPO_CONCLUIDAS);
        assertThat(lista.obterGrupoDaTarefa(titulo))
                .as("Tarefa \"%s\" concluída antes da ação", titulo)
                .hasValue(GRUPO_CONCLUIDAS);
    }

    @Quando("eu concluo a tarefa {string}")
    public void euConcluoATarefa(String titulo) {
        contexto.lista().alternarConclusao(titulo);
    }

    @Quando("eu reabro a tarefa {string}")
    public void euReabroATarefa(String titulo) {
        contexto.lista().alternarConclusao(titulo);
    }
}
