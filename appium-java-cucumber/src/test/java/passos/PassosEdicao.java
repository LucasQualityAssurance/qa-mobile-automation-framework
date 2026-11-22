package passos;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import suporte.ContextoCenario;

/**
 * Passos de edição e exclusão de uma tarefa existente: alterar o título, salvar, sair da edição
 * com alterações pendentes (continuar editando ou descartar) e excluir.
 */
public class PassosEdicao {

    private final ContextoCenario contexto;

    public PassosEdicao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que alterei o título da tarefa {string} para {string} sem salvar")
    public void queAltereiOTituloDaTarefaSemSalvar(String titulo, String novoTitulo) {
        contexto.definirEdicao(contexto.lista().abrirTarefa(titulo).informarTitulo(novoTitulo));
    }

    @Dado("que tentei sair da edição")
    public void queTenteiSairDaEdicao() {
        sairDaEdicao();
    }

    @Quando("eu tento sair da edição")
    public void euTentoSairDaEdicao() {
        sairDaEdicao();
    }

    @Quando("eu escolho continuar editando")
    public void euEscolhoContinuarEditando() {
        contexto.definirEdicao(contexto.edicao().continuarEditando());
    }

    @Quando("eu escolho descartar as alterações")
    public void euEscolhoDescartarAsAlteracoes() {
        contexto.definirLista(contexto.edicao().descartarAlteracoes());
    }

    @Quando("eu altero o título da tarefa {string} para {string} e salvo")
    public void euAlteroOTituloDaTarefaESalvo(String titulo, String novoTitulo) {
        contexto.definirLista(contexto.lista().abrirTarefa(titulo).informarTitulo(novoTitulo).salvar());
        contexto.registrarTarefaRenomeada(titulo, novoTitulo);
    }

    @Quando("eu excluo a tarefa {string}")
    public void euExcluoATarefa(String titulo) {
        contexto.definirLista(contexto.lista().abrirTarefa(titulo).excluir());
        contexto.registrarTarefaExcluida(titulo);
    }

    private void sairDaEdicao() {
        contexto.definirEdicao(contexto.edicao().voltarDaEdicao());
    }
}
