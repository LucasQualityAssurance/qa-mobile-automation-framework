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
 * <p>
 * Todas as consultas à tela esperam a condição (ver {@code BasePage}): uma tarefa que demora a
 * aparecer, ou a sumir por causa de uma animação, não gera falha falsa.
 */
public class    PassosValidacao {

    private final ContextoCenario contexto;

    /** O PicoContainer injeta o mesmo {@link ContextoCenario} em todas as classes de passos do cenário. */
    public PassosValidacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ---------- Presença e ausência na lista ----------

    /** Espera a tarefa aparecer na lista aberta no momento. */
    @Então("a tarefa {string} aparece na lista")
    public void aTarefaApareceNaLista(String titulo) {
        assertThat(contexto.lista().exibeTarefa(titulo))
                .as("Tarefa \"%s\" exibida na lista", titulo)
                .isTrue();
    }

    /**
     * Espera a tarefa sair da lista (ou confirma que ela nunca apareceu). Usa uma espera de
     * desaparecimento, e não "não está visível agora", para não passar antes da lista atualizar.
     */
    @Então("a tarefa {string} não aparece na lista")
    public void aTarefaNaoApareceNaLista(String titulo) {
        assertThat(contexto.lista().naoExibeTarefa(titulo))
                .as("Tarefa \"%s\" ausente da lista", titulo)
                .isTrue();
    }

    /** Mesma validação da ausência; a frase "deixa de aparecer" lê melhor logo após uma ação. */
    @Então("a tarefa {string} deixa de aparecer na lista")
    public void aTarefaDeixaDeAparecerNaLista(String titulo) {
        aTarefaNaoApareceNaLista(titulo);
    }

    /**
     * Confere sob qual cabeçalho de grupo a tarefa está (ex.: "Vencimento hoje", "Atrasado",
     * "Concluída"). Se o grupo estiver recolhido, ele é expandido antes da conferência.
     */
    @Então("a tarefa {string} aparece no grupo {string}")
    public void aTarefaApareceNoGrupo(String titulo, String grupo) {
        TelaListaTarefas lista = contexto.lista().localizarTarefaNoGrupo(titulo, grupo);
        assertThat(lista.obterGrupoDaTarefa(titulo))
                .as("Grupo em que a tarefa \"%s\" é exibida", titulo)
                .hasValue(grupo);
    }

    /**
     * Confere duas coisas: a subtarefa vem imediatamente depois da tarefa pai na lista e é
     * exibida com recuo maior (deslocada para a direita), como o app mostra uma subtarefa.
     */
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

    /** Abre um filtro do menu lateral (ex.: "Hoje") e confere que a tarefa está nele. */
    @Então("a tarefa {string} aparece no filtro {string}")
    public void aTarefaApareceNoFiltro(String titulo, String filtro) {
        validarTarefaNoItemDoMenu(titulo, filtro);
    }

    /** Abre uma lista criada pela pessoa (ex.: "Projetos de TI") e confere que a tarefa está nela. */
    @Então("a tarefa {string} aparece na lista {string}")
    public void aTarefaApareceNaListaDoMenu(String titulo, String nomeLista) {
        validarTarefaNoItemDoMenu(titulo, nomeLista);
    }

    /**
     * Filtros e listas abrem pelo mesmo menu: as duas frases acima são separadas só para o cenário
     * ficar claro. A tela aberta passa a ser a atual do cenário, para os passos seguintes.
     */
    private void validarTarefaNoItemDoMenu(String titulo, String itemDoMenu) {
        TelaListaTarefas lista = contexto.lista().abrirMenuLateral().selecionar(itemDoMenu);
        contexto.definirLista(lista);
        assertThat(lista.obterTituloDaTela()).as("Título da tela aberta pelo menu").isEqualTo(itemDoMenu);
        assertThat(lista.exibeTarefa(titulo))
                .as("Tarefa \"%s\" exibida em \"%s\"", titulo, itemDoMenu)
                .isTrue();
    }

    // ---------- Tela de edição ----------

    /**
     * Reabre a tarefa e confere os dados gravados: título, texto do vencimento como o app o
     * exibe (ex.: "Hoje") e o botão de prioridade selecionado. {@code {prioridade}} é convertido
     * pelo {@code TiposParametro}. Termina na tela de edição.
     */
    @Então("a tarefa {string} mantém o vencimento {string} e a prioridade {prioridade} ao ser reaberta")
    public void aTarefaMantemOsDadosAoSerReaberta(String titulo, String vencimento, Prioridade prioridade) {
        TelaEdicaoTarefa edicao = contexto.lista().abrirTarefa(titulo);
        contexto.definirEdicao(edicao);
        assertThat(edicao.obterTitulo()).as("Título gravado").isEqualTo(titulo);
        assertThat(edicao.obterTextoVencimento()).as("Vencimento gravado").isEqualTo(vencimento);
        assertThat(edicao.obterPrioridadeSelecionada()).as("Prioridade gravada").isEqualTo(prioridade);
    }

    /** Confere que o diálogo de confirmação de descarte está aberto sobre a tela de edição. */
    @Então("o aplicativo pede a confirmação para descartar as alterações")
    public void oAplicativoPedeAConfirmacaoParaDescartar() {
        assertThat(contexto.edicao().exibeDialogoDescarte())
                .as("Diálogo de confirmação de descarte exibido")
                .isTrue();
    }

    /** Lê o campo de título da tela de edição: a alteração não salva deve continuar digitada. */
    @Então("o título em edição continua {string}")
    public void oTituloEmEdicaoContinua(String titulo) {
        assertThat(contexto.edicao().obterTitulo())
                .as("Alteração mantida na tela de edição")
                .isEqualTo(titulo);
    }

    // ---------- Busca e ordenação ----------

    /**
     * Confere que a busca mostra exatamente as tarefas da tabela (sem faltar nem sobrar), em
     * qualquer ordem. Fecha a busca ao final, para o próximo passo partir da lista completa.
     */
    @Então("o resultado da busca contém somente as tarefas:")
    public void oResultadoDaBuscaContemSomenteAsTarefas(List<String> tarefas) {
        assertThat(contexto.lista().obterTitulosDasTarefas())
                .as("Tarefas exibidas no resultado da busca")
                .containsExactlyInAnyOrderElementsOf(tarefas);
        contexto.lista().fecharBusca();
    }

    /**
     * Confere a mensagem de lista vazia ("Não há tarefas aqui.") da busca aberta e fecha a busca.
     * Depende de um passo anterior ter feito a busca.
     */
    @Então("a busca não encontra tarefas")
    public void aBuscaNaoEncontraTarefas() {
        assertThat(contexto.lista().estaVazia())
                .as("Mensagem \"Não há tarefas aqui.\" exibida na busca")
                .isTrue();
        contexto.lista().fecharBusca();
    }

    /** Faz a busca e confere que nada é encontrado, numa frase só. Usado após excluir uma tarefa. */
    @Então("a busca por {string} não encontra tarefas")
    public void aBuscaPorNaoEncontraTarefas(String termo) {
        contexto.lista().buscar(termo);
        aBuscaNaoEncontraTarefas();
    }

    /** Considera somente as tarefas informadas, preservando a ordem em que aparecem na tela. */
    @Então("as tarefas aparecem na ordem {string}, {string} e {string}")
    public void asTarefasAparecemNaOrdem(String primeira, String segunda, String terceira) {
        List<String> esperadas = List.of(primeira, segunda, terceira);
        // Filtra as demais tarefas do aparelho: só a posição relativa das três esperadas importa.
        List<String> exibidas = contexto.lista().obterTitulosDasTarefas().stream()
                .filter(esperadas::contains)
                .toList();
        assertThat(exibidas).as("Ordem das tarefas na lista").containsExactlyElementsOf(esperadas);
    }
}
