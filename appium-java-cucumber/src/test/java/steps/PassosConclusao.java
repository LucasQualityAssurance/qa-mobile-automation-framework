package steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import support.ContextoCenario;

/**
 * Passos de conclusão e reabertura de tarefas e da preferência "Mostrar tarefas concluídas".
 * <p>
 * A preferência fica gravada no aplicativo. Quando um cenário oculta as concluídas, isso é
 * registrado no {@link ContextoCenario} e o hook de limpeza volta a exibi-las ao final.
 * <p>
 * Usados em {@code ciclo_vida_tarefa.feature}, no cenário "Concluir, reabrir e excluir a tarefa".
 */
public class PassosConclusao {

    private final ContextoCenario contexto;

    /** O PicoContainer injeta o mesmo {@link ContextoCenario} em todas as classes de passos do cenário. */
    public PassosConclusao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /**
     * Desliga "Mostrar tarefas concluídas" no painel de ordenação. Com isso, uma tarefa concluída
     * some da lista em vez de ir para o grupo "Concluída".
     * <p>
     * Registra a alteração para que a limpeza religue a opção ao final, mesmo se o cenário falhar.
     */
    @Dado("que as tarefas concluídas estão ocultas")
    public void queAsTarefasConcluidasEstaoOcultas() {
        contexto.registrarExibicaoConcluidasAlterada();
        definirMostrarConcluidas(false);
    }

    /**
     * Liga "Mostrar tarefas concluídas": as tarefas concluídas voltam a aparecer, agrupadas em
     * "Concluída" (grupo que começa recolhido; o passo de validação o expande se preciso).
     */
    @Quando("eu passo a exibir as tarefas concluídas")
    public void euPassoAExibirAsTarefasConcluidas() {
        definirMostrarConcluidas(true);
    }

    /** Toca no círculo à esquerda do título: a tarefa fica concluída. */
    @Quando("eu concluo a tarefa {string}")
    public void euConcluoATarefa(String titulo) {
        contexto.lista().alternarConclusao(titulo);
    }

    /**
     * Toca de novo no círculo de uma tarefa concluída: ela volta a ficar ativa. É o mesmo toque
     * da conclusão; o nome diferente deixa o cenário legível.
     */
    @Quando("eu reabro a tarefa {string}")
    public void euReabroATarefa(String titulo) {
        contexto.lista().alternarConclusao(titulo);
    }

    /** Abre o painel de ordenação, ajusta o interruptor (só toca se precisar mudar) e fecha o painel. */
    private void definirMostrarConcluidas(boolean mostrar) {
        contexto.definirLista(contexto.lista().abrirOrdenacao().definirMostrarConcluidas(mostrar).fechar());
    }
}
