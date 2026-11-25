package steps;

import io.cucumber.java.pt.Então;
import pages.Prioridade;
import pages.TelaEdicaoTarefa;
import pages.TelaListaTarefas;
import support.ContextoCenario;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos {@code Então}: as validações do resultado esperado de cada cenário.
 * <p>
 * Cada validação traz uma descrição ({@code as(...)}) para que a mensagem de falha no terminal e
 * no Allure explique o que era esperado.
 */
public class PassosValidacao {

    private final ContextoCenario contexto;

    public PassosValidacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ---------- Presença e ausência na lista ----------

    @Então("a tarefa {string} aparece na lista")
    public void aTarefaApareceNaLista(String titulo) {
        assertThat(contexto.lista().exibeTarefa(titulo))
                .as("Tarefa \"%s\" exibida na lista", titulo)
                .isTrue();
    }

    @Então("a tarefa {string} não aparece na lista")
    public void aTarefaNaoApareceNaLista(String titulo) {
        assertThat(contexto.lista().naoExibeTarefa(titulo))
                .as("Tarefa \"%s\" ausente da lista", titulo)
                .isTrue();
    }

    @Então("a tarefa {string} deixa de aparecer na lista")
    public void aTarefaDeixaDeAparecerNaLista(String titulo) {
        aTarefaNaoApareceNaLista(titulo);
    }

    @Então("a tarefa {string} aparece no grupo {string}")
    public void aTarefaApareceNoGrupo(String titulo, String grupo) {
        TelaListaTarefas lista = contexto.lista().localizarTarefaNoGrupo(titulo, grupo);
        assertThat(lista.obterGrupoDaTarefa(titulo))
                .as("Grupo em que a tarefa \"%s\" é exibida", titulo)
                .hasValue(grupo);
    }

    @Então("a subtarefa {string} aparece logo abaixo da tarefa {string}")
    public void aSubtarefaApareceLogoAbaixoDaTarefa(String subtarefa, String tarefa) {
        TelaListaTarefas lista = contexto.lista();
        assertThat(lista.obterTitulosDasTarefas())
                .as("Subtarefa exibida imediatamente após a tarefa pai")
                .containsSequence(tarefa, subtarefa);
        assertThat(lista.obterRecuoDaTarefa(subtarefa))
                .as("Subtarefa exibida com recuo sob a tarefa pai")
                .isGreaterThan(lista.obterRecuoDaTarefa(tarefa));
    }

    // ---------- Filtros e listas do menu lateral ----------

    @Então("a tarefa {string} aparece no filtro {string}")
    public void aTarefaApareceNoFiltro(String titulo, String filtro) {
        validarTarefaNoItemDoMenu(titulo, filtro);
    }

    @Então("a tarefa {string} aparece na lista {string}")
    public void aTarefaApareceNaListaDoMenu(String titulo, String nomeLista) {
        validarTarefaNoItemDoMenu(titulo, nomeLista);
    }

    private void validarTarefaNoItemDoMenu(String titulo, String itemDoMenu) {
        TelaListaTarefas lista = contexto.lista().abrirMenuLateral().selecionar(itemDoMenu);
        contexto.definirLista(lista);
        assertThat(lista.obterTituloDaTela()).as("Título da tela aberta pelo menu").isEqualTo(itemDoMenu);
        assertThat(lista.exibeTarefa(titulo))
                .as("Tarefa \"%s\" exibida em \"%s\"", titulo, itemDoMenu)
                .isTrue();
    }

    // ---------- Tela de edição ----------

    @Então("a tarefa {string} mantém o vencimento {string} e a prioridade {prioridade} ao ser reaberta")
    public void aTarefaMantemOsDadosAoSerReaberta(String titulo, String vencimento, Prioridade prioridade) {
        TelaEdicaoTarefa edicao = contexto.lista().abrirTarefa(titulo);
        contexto.definirEdicao(edicao);
        assertThat(edicao.obterTitulo()).as("Título gravado").isEqualTo(titulo);
        assertThat(edicao.obterTextoVencimento()).as("Vencimento gravado").isEqualTo(vencimento);
        assertThat(edicao.obterPrioridadeSelecionada()).as("Prioridade gravada").isEqualTo(prioridade);
    }

    @Então("o aplicativo pede a confirmação para descartar as alterações")
    public void oAplicativoPedeAConfirmacaoParaDescartar() {
        assertThat(contexto.edicao().exibeDialogoDescarte())
                .as("Diálogo de confirmação de descarte exibido")
                .isTrue();
    }

    @Então("o título em edição continua {string}")
    public void oTituloEmEdicaoContinua(String titulo) {
        assertThat(contexto.edicao().obterTitulo())
                .as("Alteração mantida na tela de edição")
                .isEqualTo(titulo);
    }

    // ---------- Busca e ordenação ----------

    @Então("o resultado da busca contém somente as tarefas:")
    public void oResultadoDaBuscaContemSomenteAsTarefas(List<String> tarefas) {
        assertThat(contexto.lista().obterTitulosDasTarefas())
                .as("Tarefas exibidas no resultado da busca")
                .containsExactlyInAnyOrderElementsOf(tarefas);
        contexto.lista().fecharBusca();
    }

    @Então("a busca não encontra tarefas")
    public void aBuscaNaoEncontraTarefas() {
        assertThat(contexto.lista().estaVazia())
                .as("Mensagem \"Não há tarefas aqui.\" exibida na busca")
                .isTrue();
        contexto.lista().fecharBusca();
    }

    @Então("a busca por {string} não encontra tarefas")
    public void aBuscaPorNaoEncontraTarefas(String termo) {
        contexto.lista().buscar(termo);
        aBuscaNaoEncontraTarefas();
    }

    /** Considera somente as tarefas informadas, preservando a ordem em que aparecem na tela. */
    @Então("as tarefas aparecem na ordem {string}, {string} e {string}")
    public void asTarefasAparecemNaOrdem(String primeira, String segunda, String terceira) {
        List<String> esperadas = List.of(primeira, segunda, terceira);
        List<String> exibidas = contexto.lista().obterTitulosDasTarefas().stream()
                .filter(esperadas::contains)
                .toList();
        assertThat(exibidas).as("Ordem das tarefas na lista").containsExactlyElementsOf(esperadas);
    }
}
