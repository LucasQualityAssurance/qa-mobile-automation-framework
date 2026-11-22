package pages;

import config.Configuracao;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Tela principal do aplicativo: a lista de tarefas.
 * <p>
 * <b>Quando aparece:</b> ao abrir o app, ao salvar ou excluir uma tarefa e ao escolher um filtro
 * ou lista no menu lateral. É a tela em que todo cenário começa (o hook {@code @Before} garante isso).
 * <p>
 * <b>O que tem na tela:</b>
 * <ul>
 *   <li>barra superior com o nome da lista atual ("Minhas tarefas", "Hoje", "Projetos de TI"...);</li>
 *   <li>as tarefas, agrupadas por cabeçalhos como "Atrasado", "Vencimento hoje",
 *       "Sem data de vencimento" e "Concluída". Subtarefas aparecem com recuo, logo abaixo da tarefa pai;</li>
 *   <li>barra inferior com o botão do menu lateral, a busca e a ordenação;</li>
 *   <li>botão flutuante "+" ({@code fab}) para criar uma tarefa.</li>
 * </ul>
 * <b>Para onde vai:</b> {@link TelaEdicaoTarefa} (nova tarefa ou tocar em uma tarefa),
 * {@link MenuLateral} e {@link PainelOrdenacao}. A busca acontece nesta mesma tela.
 */
public class TelaListaTarefas extends BasePage {

    /** Nome da lista exibido na barra superior. */
    private static final By TITULO_TELA = By.xpath(
            "//*[@resource-id='org.tasks:id/toolbar']//android.widget.TextView");
    /** Botão "+" de nova tarefa. Também serve para confirmar que a lista carregou. */
    private static final By BOTAO_NOVA_TAREFA = AppiumBy.id("org.tasks:id/fab");
    /** Ícone de menu (três linhas) no canto esquerdo da barra inferior. Não tem id próprio. */
    private static final By BOTAO_MENU = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"org.tasks:id/bottomAppBar\")"
                    + ".childSelector(new UiSelector().className(\"android.widget.ImageButton\"))");
    private static final By BOTAO_BUSCAR = AppiumBy.id("org.tasks:id/menu_search");
    private static final By BOTAO_ORDENAR = AppiumBy.id("org.tasks:id/menu_sort");
    /** Campo de texto que aparece na barra superior quando a busca está aberta. */
    private static final By CAMPO_BUSCA = AppiumBy.id("org.tasks:id/search_src_text");
    /** Seta que fecha a busca e volta à lista completa. */
    private static final By BOTAO_RECOLHER_BUSCA = porDescricao("Recolher");
    /** Título de cada tarefa (e subtarefa) exibida. */
    private static final By TITULOS_TAREFAS = AppiumBy.id("org.tasks:id/title");
    /** Mensagem exibida quando a lista, ou o resultado da busca, está vazia. */
    private static final By MENSAGEM_LISTA_VAZIA = porTexto("Não há tarefas aqui.");
    /** Aviso temporário no rodapé, como "Tarefa excluída". */
    private static final By MENSAGEM_SNACKBAR = AppiumBy.id("org.tasks:id/snackbar_text");
    /** Cabeçalhos de grupo e títulos de tarefa juntos, na ordem em que aparecem na tela. */
    private static final By TITULOS_E_GRUPOS = By.xpath(
            "//*[@resource-id='org.tasks:id/header' or @resource-id='org.tasks:id/title']");

    private static By tituloDaTarefa(String titulo) {
        return By.xpath("//*[@resource-id='org.tasks:id/title' and @text=" + literalXpath(titulo) + "]");
    }

    /** Círculo de conclusão à esquerda do título da tarefa (irmão do título na mesma linha). */
    private static By caixaConclusao(String titulo) {
        return By.xpath("//*[@resource-id='org.tasks:id/title' and @text=" + literalXpath(titulo) + "]"
                + "/../*[@resource-id='org.tasks:id/completeBox']");
    }

    /** Linha inteira do cabeçalho de grupo: tocar nela expande ou recolhe o grupo. */
    private static By cabecalhoDoGrupo(String grupo) {
        return By.xpath("//*[@resource-id='org.tasks:id/header' and @text=" + literalXpath(grupo) + "]/..");
    }

    /** Espera o botão "+" aparecer, sinal de que a lista está pronta para uso. */
    public TelaListaTarefas aguardarCarregamento() {
        esperarVisivel(BOTAO_NOVA_TAREFA);
        return this;
    }

    // ---------- Consultas (usadas nas validações dos testes) ----------

    /** Nome da lista ou filtro aberto no momento, por exemplo "Minhas tarefas". */
    public String obterTituloDaTela() {
        return obterTexto(TITULO_TELA);
    }

    /** {@code true} quando a tela mostra "Não há tarefas aqui.". */
    public boolean estaVazia() {
        return estaVisivel(MENSAGEM_LISTA_VAZIA);
    }

    public boolean exibeTarefa(String titulo) {
        return estaVisivel(tituloDaTarefa(titulo));
    }

    /** Espera a tarefa sair da lista, por exemplo depois de concluída com as concluídas ocultas. */
    public void aguardarTarefaSumir(String titulo) {
        esperarDesaparecer(tituloDaTarefa(titulo));
    }

    /**
     * {@code true} quando a tarefa não aparece na lista dentro do tempo padrão. Aguarda a saída
     * da tarefa em vez de olhar só o instante atual, o que evita aprovar a validação antes de a
     * lista ser atualizada.
     */
    public boolean naoExibeTarefa(String titulo) {
        return estaAusente(tituloDaTarefa(titulo));
    }

    /** {@code true} quando o cabeçalho do grupo (ex.: "Concluída") está na tela. */
    public boolean exibeGrupo(String grupo) {
        return estaVisivel(cabecalhoDoGrupo(grupo));
    }

    /**
     * Espera a tarefa aparecer sob o cabeçalho do grupo informado. Se o grupo estiver recolhido
     * (a tarefa não aparece em lugar nenhum, mas o cabeçalho sim), expande o grupo antes.
     * <p>
     * Não falha: depois de chamar, confira o resultado com {@link #obterGrupoDaTarefa(String)}.
     */
    @Step("Localizar a tarefa \"{titulo}\" no grupo \"{grupo}\"")
    public TelaListaTarefas localizarTarefaNoGrupo(String titulo, String grupo) {
        if (estaNoGrupo(titulo, grupo, Configuracao.esperaCurta())) {
            return this;
        }
        if (exibeGrupo(grupo) && obterGrupoDaTarefa(titulo).isEmpty()) {
            expandirGrupo(grupo);
        }
        estaNoGrupo(titulo, grupo, Configuracao.esperaPadrao());
        return this;
    }

    private boolean estaNoGrupo(String titulo, String grupo, Duration tempo) {
        return aguardarCondicao(() -> obterGrupoDaTarefa(titulo).filter(grupo::equals).isPresent(), tempo);
    }

    /**
     * Títulos das tarefas visíveis, na ordem da tela, incluindo subtarefas.
     * Considera só o que está na área visível: tarefas fora da tela, sem rolagem, não entram.
     */
    public List<String> obterTitulosDasTarefas() {
        return buscarTodos(TITULOS_TAREFAS).stream().map(WebElement::getText).toList();
    }

    public String obterMensagemSnackbar() {
        return obterTexto(MENSAGEM_SNACKBAR);
    }

    /** Posição horizontal do título; subtarefas são exibidas com recuo em relação à tarefa pai. */
    public int obterRecuoDaTarefa(String titulo) {
        return esperarVisivel(tituloDaTarefa(titulo)).getRect().getX();
    }

    /**
     * Retorna o nome do grupo (cabeçalho) em que a tarefa está exibida,
     * por exemplo "Atrasado", "Vencimento hoje" ou "Concluída".
     * <p>
     * Percorre cabeçalhos e títulos na ordem da tela: o grupo da tarefa é o último
     * cabeçalho encontrado antes dela.
     */
    public Optional<String> obterGrupoDaTarefa(String titulo) {
        String grupoAtual = null;
        for (WebElement elemento : buscarTodos(TITULOS_E_GRUPOS)) {
            String texto = elemento.getText();
            if (elemento.getAttribute("resource-id").endsWith("/header")) {
                grupoAtual = texto;
            } else if (texto.equals(titulo)) {
                return Optional.ofNullable(grupoAtual);
            }
        }
        return Optional.empty();
    }

    // ---------- Ações ----------

    /** Toca no botão "+" e abre o formulário de nova tarefa. */
    @Step("Tocar em criar nova tarefa")
    public TelaEdicaoTarefa tocarEmNovaTarefa() {
        clicar(BOTAO_NOVA_TAREFA);
        return new TelaEdicaoTarefa().aguardarCarregamento();
    }

    /** Atalho para criar uma tarefa só com o título, sem os demais campos. Útil em pré-condições. */
    @Step("Criar a tarefa \"{titulo}\"")
    public TelaListaTarefas criarTarefa(String titulo) {
        return tocarEmNovaTarefa().informarTitulo(titulo).salvar();
    }

    /** Toca no título de uma tarefa existente e abre a tela de edição dela. */
    @Step("Abrir a tarefa \"{titulo}\"")
    public TelaEdicaoTarefa abrirTarefa(String titulo) {
        clicar(tituloDaTarefa(titulo));
        return new TelaEdicaoTarefa().aguardarCarregamento();
    }

    /** Toca no círculo da tarefa: conclui uma tarefa aberta ou reabre uma concluída. */
    @Step("Marcar/desmarcar a conclusão da tarefa \"{titulo}\"")
    public TelaListaTarefas alternarConclusao(String titulo) {
        clicar(caixaConclusao(titulo));
        return this;
    }

    /** Toca no cabeçalho do grupo. O grupo "Concluída", por exemplo, começa recolhido. */
    @Step("Expandir o grupo \"{grupo}\"")
    public TelaListaTarefas expandirGrupo(String grupo) {
        clicar(cabecalhoDoGrupo(grupo));
        return this;
    }

    @Step("Abrir o menu lateral")
    public MenuLateral abrirMenuLateral() {
        clicar(BOTAO_MENU);
        return new MenuLateral().aguardarAbertura();
    }

    @Step("Abrir as opções de ordenação")
    public PainelOrdenacao abrirOrdenacao() {
        clicar(BOTAO_ORDENAR);
        return new PainelOrdenacao().aguardarAbertura();
    }

    /**
     * Abre a busca e digita o termo; a lista é filtrada enquanto se digita.
     * Depois de validar o resultado, feche com {@link #fecharBusca()}.
     */
    @Step("Buscar pelo termo \"{termo}\"")
    public TelaListaTarefas buscar(String termo) {
        clicar(BOTAO_BUSCAR);
        digitar(CAMPO_BUSCA, termo);
        esconderTeclado();
        return this;
    }

    @Step("Fechar a busca")
    public TelaListaTarefas fecharBusca() {
        // Usa o botão da barra: a tecla "voltar" pode fechar o aplicativo
        clicar(BOTAO_RECOLHER_BUSCA);
        esperarDesaparecer(CAMPO_BUSCA);
        return aguardarCarregamento();
    }

    /** Monta um literal XPath seguro mesmo quando o texto contém aspas. */
    private static String literalXpath(String texto) {
        if (!texto.contains("'")) {
            return "'" + texto + "'";
        }
        return "concat('" + texto.replace("'", "', \"'\", '") + "')";
    }
}
