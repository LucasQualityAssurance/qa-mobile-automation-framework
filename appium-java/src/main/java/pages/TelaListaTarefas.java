package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

/** Lista de tarefas (tela principal), incluindo a barra inferior e a busca. */
public class TelaListaTarefas extends BasePage {

    private static final By TITULO_TELA = By.xpath(
            "//*[@resource-id='org.tasks:id/toolbar']//android.widget.TextView");
    private static final By BOTAO_NOVA_TAREFA = AppiumBy.id("org.tasks:id/fab");
    private static final By BOTAO_MENU = AppiumBy.androidUIAutomator(
            "new UiSelector().resourceId(\"org.tasks:id/bottomAppBar\")"
                    + ".childSelector(new UiSelector().className(\"android.widget.ImageButton\"))");
    private static final By BOTAO_BUSCAR = AppiumBy.id("org.tasks:id/menu_search");
    private static final By BOTAO_ORDENAR = AppiumBy.id("org.tasks:id/menu_sort");
    private static final By CAMPO_BUSCA = AppiumBy.id("org.tasks:id/search_src_text");
    private static final By BOTAO_RECOLHER_BUSCA = porDescricao("Recolher");
    private static final By TITULOS_TAREFAS = AppiumBy.id("org.tasks:id/title");
    private static final By MENSAGEM_LISTA_VAZIA = porTexto("Não há tarefas aqui.");
    private static final By MENSAGEM_SNACKBAR = AppiumBy.id("org.tasks:id/snackbar_text");
    private static final By TITULOS_E_GRUPOS = By.xpath(
            "//*[@resource-id='org.tasks:id/header' or @resource-id='org.tasks:id/title']");

    private static By tituloDaTarefa(String titulo) {
        return By.xpath("//*[@resource-id='org.tasks:id/title' and @text=" + literalXpath(titulo) + "]");
    }

    private static By caixaConclusao(String titulo) {
        return By.xpath("//*[@resource-id='org.tasks:id/title' and @text=" + literalXpath(titulo) + "]"
                + "/../*[@resource-id='org.tasks:id/completeBox']");
    }

    private static By cabecalhoDoGrupo(String grupo) {
        return By.xpath("//*[@resource-id='org.tasks:id/header' and @text=" + literalXpath(grupo) + "]/..");
    }

    public TelaListaTarefas aguardarCarregamento() {
        esperarVisivel(BOTAO_NOVA_TAREFA);
        return this;
    }

    // ---------- Consultas ----------

    public String obterTituloDaTela() {
        return obterTexto(TITULO_TELA);
    }

    public boolean estaVazia() {
        return estaVisivel(MENSAGEM_LISTA_VAZIA);
    }

    public boolean exibeTarefa(String titulo) {
        return estaVisivel(tituloDaTarefa(titulo));
    }

    public void aguardarTarefaSumir(String titulo) {
        esperarDesaparecer(tituloDaTarefa(titulo));
    }

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

    @Step("Tocar em criar nova tarefa")
    public TelaEdicaoTarefa tocarEmNovaTarefa() {
        clicar(BOTAO_NOVA_TAREFA);
        return new TelaEdicaoTarefa().aguardarCarregamento();
    }

    @Step("Criar a tarefa \"{titulo}\"")
    public TelaListaTarefas criarTarefa(String titulo) {
        return tocarEmNovaTarefa().informarTitulo(titulo).salvar();
    }

    @Step("Abrir a tarefa \"{titulo}\"")
    public TelaEdicaoTarefa abrirTarefa(String titulo) {
        clicar(tituloDaTarefa(titulo));
        return new TelaEdicaoTarefa().aguardarCarregamento();
    }

    @Step("Marcar/desmarcar a conclusão da tarefa \"{titulo}\"")
    public TelaListaTarefas alternarConclusao(String titulo) {
        clicar(caixaConclusao(titulo));
        return this;
    }

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
