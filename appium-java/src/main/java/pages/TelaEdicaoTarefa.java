package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;

/**
 * Tela de criação e edição de tarefa. É a mesma tela nos dois casos.
 * <p>
 * <b>Quando aparece:</b> ao tocar no "+" da lista (nova tarefa) ou no título de uma tarefa
 * existente (edição).
 * <p>
 * <b>O que tem na tela</b>, de cima para baixo:
 * <ul>
 *   <li>botões de salvar e de excluir a tarefa;</li>
 *   <li>campo de título;</li>
 *   <li>linha "Data de vencimento", que abre o {@link SeletorData};</li>
 *   <li>quatro botões de prioridade: nenhuma, baixa, média e alta (ver {@link Prioridade});</li>
 *   <li>linha da lista, que abre o {@link SeletorLista};</li>
 *   <li>"Adicionar sub-tarefa", que cria um campo de texto para cada subtarefa.</li>
 * </ul>
 * <b>Comportamentos importantes:</b>
 * <ul>
 *   <li>Ao voltar com alterações não salvas, o app pergunta se deseja descartá-las
 *       ("Descartar" ou "Continuar editando").</li>
 *   <li>O teclado costuma ficar aberto depois de digitar e cobre os campos de baixo. Por isso
 *       os métodos chamam {@code esconderTeclado()} antes de tocar em outro campo.</li>
 * </ul>
 * <b>Para onde vai:</b> volta para a {@link TelaListaTarefas} ao salvar, descartar ou excluir.
 */
public class TelaEdicaoTarefa extends BasePage {

    private static final By CAMPO_TITULO = AppiumBy.id("org.tasks:id/task_title");
    /** Botão de salvar (testTag do Compose, por isso sem o prefixo org.tasks:id/). */
    private static final By BOTAO_SALVAR = AppiumBy.id("save_task");
    /** Linha "Data de vencimento". O texto dentro dela mostra a data escolhida ("Hoje", "Amanhã"...). */
    private static final By LINHA_VENCIMENTO = AppiumBy.id("due_date_row");
    /** Os quatro botões de prioridade, na ordem da esquerda para a direita. */
    private static final By BOTOES_PRIORIDADE = AppiumBy.className("android.widget.RadioButton");
    /** Linha da lista: a linha clicável que contém o ícone de lista ({@code content-desc='list'}). */
    private static final By SELETOR_LISTA = By.xpath(
            "//android.view.View[@clickable='true'][android.view.View[@content-desc='list']]");
    private static final By ADICIONAR_SUBTAREFA = porTexto("Adicionar sub-tarefa");
    /** Campo novo, ainda vazio, criado ao tocar em "Adicionar sub-tarefa". */
    private static final By CAMPO_SUBTAREFA_VAZIO = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.EditText\").text(\"\")");
    private static final By BOTAO_EXCLUIR = porDescricao("Excluir tarefa");
    /** Diálogo exibido ao voltar com alterações não salvas. */
    private static final By DIALOGO_DESCARTE = porTexto("Você tem certeza que deseja descartar suas mudanças?");
    private static final By BOTAO_DESCARTAR = porTexto("Descartar");
    private static final By BOTAO_CONTINUAR_EDITANDO = porTexto("Continuar editando");
    /** Confirmação do diálogo de exclusão. */
    private static final By BOTAO_OK = porTexto("OK");

    /** Espera o campo de título aparecer, sinal de que o formulário abriu. */
    public TelaEdicaoTarefa aguardarCarregamento() {
        esperarVisivel(CAMPO_TITULO);
        return this;
    }

    // ---------- Consultas (usadas nas validações dos testes) ----------

    public boolean estaAberta() {
        return estaVisivel(CAMPO_TITULO);
    }

    public String obterTitulo() {
        return obterTexto(CAMPO_TITULO);
    }

    /** Texto exibido na linha de vencimento, por exemplo "Hoje". */
    public String obterTextoVencimento() {
        return esperarVisivel(LINHA_VENCIMENTO).findElement(AppiumBy.className("android.widget.TextView")).getText();
    }

    /** Descobre a prioridade marcada pela posição do botão com {@code checked=true}. */
    public Prioridade obterPrioridadeSelecionada() {
        esconderTeclado();
        List<WebElement> botoes = buscarTodos(BOTOES_PRIORIDADE);
        for (int i = 0; i < botoes.size(); i++) {
            if (Boolean.parseBoolean(botoes.get(i).getAttribute("checked"))) {
                return Prioridade.daPosicao(i);
            }
        }
        throw new IllegalStateException("Nenhuma prioridade selecionada na tela de edição.");
    }

    public boolean exibeDialogoDescarte() {
        return estaVisivel(DIALOGO_DESCARTE);
    }

    // ---------- Ações ----------

    /** Substitui o título atual pelo informado. */
    @Step("Informar o título \"{titulo}\"")
    public TelaEdicaoTarefa informarTitulo(String titulo) {
        digitar(CAMPO_TITULO, titulo);
        return this;
    }

    /** Abre o seletor de data, usa o atalho "Hoje" e confirma. */
    @Step("Definir o vencimento para hoje")
    public TelaEdicaoTarefa definirVencimentoHoje() {
        esconderTeclado();
        clicar(LINHA_VENCIMENTO);
        new SeletorData().aguardarAbertura().selecionarHoje().confirmar();
        return this;
    }

    /** Abre o seletor de data, navega no calendário até a data e confirma. */
    @Step("Definir o vencimento para {data}")
    public TelaEdicaoTarefa definirVencimento(LocalDate data) {
        esconderTeclado();
        clicar(LINHA_VENCIMENTO);
        new SeletorData().aguardarAbertura().selecionarData(data).confirmar();
        return this;
    }

    @Step("Definir a prioridade {prioridade}")
    public TelaEdicaoTarefa definirPrioridade(Prioridade prioridade) {
        esconderTeclado();
        esperarVisivel(BOTOES_PRIORIDADE);
        buscarTodos(BOTOES_PRIORIDADE).get(prioridade.posicao()).click();
        return this;
    }

    /** Abre o diálogo de listas e escolhe a lista à qual a tarefa vai pertencer. */
    @Step("Selecionar a lista \"{nomeLista}\"")
    public TelaEdicaoTarefa selecionarLista(String nomeLista) {
        esconderTeclado();
        clicar(SELETOR_LISTA);
        new SeletorLista().aguardarAbertura().selecionar(nomeLista);
        return this;
    }

    /** Cria um campo de subtarefa e digita o título. A subtarefa só é gravada ao salvar a tarefa. */
    @Step("Adicionar a subtarefa \"{titulo}\"")
    public TelaEdicaoTarefa adicionarSubtarefa(String titulo) {
        esconderTeclado();
        clicar(ADICIONAR_SUBTAREFA);
        esperarVisivel(CAMPO_SUBTAREFA_VAZIO).sendKeys(titulo);
        return this;
    }

    /** Salva e espera a volta para a lista. */
    @Step("Salvar a tarefa")
    public TelaListaTarefas salvar() {
        esconderTeclado();
        clicar(BOTAO_SALVAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }

    /**
     * Toca em salvar sem esperar a lista. Use em cenários em que o app não deve sair da
     * tela, como tentar salvar uma tarefa sem título.
     */
    @Step("Tentar salvar a tarefa")
    public void tocarEmSalvar() {
        esconderTeclado();
        clicar(BOTAO_SALVAR);
    }

    /**
     * Pressiona "voltar". Com alterações pendentes, o diálogo de descarte aparece e a tela
     * continua aberta; em seguida use {@link #descartarAlteracoes()} ou {@link #continuarEditando()}.
     */
    @Step("Voltar da tela de edição")
    public TelaEdicaoTarefa voltarDaEdicao() {
        voltar();
        return this;
    }

    /** No diálogo de descarte, escolhe "Descartar": as alterações são perdidas. */
    @Step("Descartar as alterações")
    public TelaListaTarefas descartarAlteracoes() {
        clicar(BOTAO_DESCARTAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }

    /** No diálogo de descarte, escolhe "Continuar editando": as alterações são mantidas na tela. */
    @Step("Continuar editando")
    public TelaEdicaoTarefa continuarEditando() {
        clicar(BOTAO_CONTINUAR_EDITANDO);
        return aguardarCarregamento();
    }

    /** Toca em "Excluir tarefa" e confirma com "OK". A tarefa e as subtarefas dela são removidas. */
    @Step("Excluir a tarefa")
    public TelaListaTarefas excluir() {
        esconderTeclado();
        clicar(BOTAO_EXCLUIR);
        clicar(BOTAO_OK);
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
