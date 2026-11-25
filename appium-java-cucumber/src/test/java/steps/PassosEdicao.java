package steps;

import io.cucumber.java.pt.Quando;
import support.ContextoCenario;

/**
 * Passos de edição e exclusão de uma tarefa existente: alterar o título, salvar, sair da edição
 * com alterações pendentes (continuar editando ou descartar) e excluir.
 * <p>
 * <b>Como o estado circula:</b> os passos que deixam a tela de edição aberta guardam a tela em
 * {@code contexto.definirEdicao(...)}; os que voltam para a lista guardam a lista em
 * {@code contexto.definirLista(...)}. O passo seguinte sempre continua da tela guardada.
 * <p>
 * Usados em {@code ciclo_vida_tarefa.feature}.
 */
public class PassosEdicao {

    private final ContextoCenario contexto;

    /** O PicoContainer injeta o mesmo {@link ContextoCenario} em todas as classes de passos do cenário. */
    public PassosEdicao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /**
     * Abre a tarefa e digita o novo título, <b>sem salvar</b>.
     * <p>
     * Termina na tela de edição, com a alteração pendente: é o ponto de partida para testar o
     * diálogo de descarte ("eu tento sair da edição").
     */
    @Quando("eu altero o título da tarefa {string} para {string} sem salvar")
    public void euAlteroOTituloDaTarefaSemSalvar(String titulo, String novoTitulo) {
        contexto.definirEdicao(contexto.lista().abrirTarefa(titulo).informarTitulo(novoTitulo));
    }

    /**
     * Pressiona "voltar" na tela de edição. Com alterações pendentes, o app não sai da tela:
     * abre o diálogo perguntando se a pessoa quer descartar ou continuar editando.
     */
    @Quando("eu tento sair da edição")
    public void euTentoSairDaEdicao() {
        contexto.definirEdicao(contexto.edicao().voltarDaEdicao());
    }

    /** No diálogo de descarte, escolhe continuar: fecha o diálogo e mantém o texto digitado. */
    @Quando("eu escolho continuar editando")
    public void euEscolhoContinuarEditando() {
        contexto.definirEdicao(contexto.edicao().continuarEditando());
    }

    /**
     * No diálogo de descarte, escolhe descartar: a edição fecha sem gravar e o app volta para
     * a lista, onde a tarefa mantém o título original.
     */
    @Quando("eu escolho descartar as alterações")
    public void euEscolhoDescartarAsAlteracoes() {
        contexto.definirLista(contexto.edicao().descartarAlteracoes());
    }

    /**
     * Abre a tarefa, digita o novo título e salva. Termina na lista.
     * <p>
     * Atualiza o registro da massa para o novo título: é com ele que a limpeza vai encontrar a
     * tarefa ao final do cenário.
     */
    @Quando("eu altero o título da tarefa {string} para {string} e salvo")
    public void euAlteroOTituloDaTarefaESalvo(String titulo, String novoTitulo) {
        contexto.definirLista(contexto.lista().abrirTarefa(titulo).informarTitulo(novoTitulo).salvar());
        contexto.registrarTarefaRenomeada(titulo, novoTitulo);
    }

    /**
     * Abre a tarefa e a exclui pelo menu da tela de edição. Termina na lista.
     * <p>
     * Remove a tarefa do registro da massa: como o próprio cenário já a excluiu, a limpeza não
     * precisa procurá-la.
     */
    @Quando("eu excluo a tarefa {string}")
    public void euExcluoATarefa(String titulo) {
        contexto.definirLista(contexto.lista().abrirTarefa(titulo).excluir());
        contexto.registrarTarefaExcluida(titulo);
    }
}
