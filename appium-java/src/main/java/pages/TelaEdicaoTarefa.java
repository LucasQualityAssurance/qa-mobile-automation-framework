package pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;

/** Tela de criação e edição de tarefa. */
public class TelaEdicaoTarefa extends BasePage {

    private static final By CAMPO_TITULO = AppiumBy.id("org.tasks:id/task_title");
    private static final By BOTAO_SALVAR = AppiumBy.id("save_task");
    private static final By LINHA_VENCIMENTO = AppiumBy.id("due_date_row");
    private static final By BOTOES_PRIORIDADE = AppiumBy.className("android.widget.RadioButton");
    private static final By SELETOR_LISTA = By.xpath(
            "//android.view.View[@clickable='true'][android.view.View[@content-desc='list']]");
    private static final By ADICIONAR_SUBTAREFA = porTexto("Adicionar sub-tarefa");
    private static final By CAMPO_SUBTAREFA_VAZIO = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.EditText\").text(\"\")");
    private static final By BOTAO_EXCLUIR = porDescricao("Excluir tarefa");
    private static final By DIALOGO_DESCARTE = porTexto("Você tem certeza que deseja descartar suas mudanças?");
    private static final By BOTAO_DESCARTAR = porTexto("Descartar");
    private static final By BOTAO_CONTINUAR_EDITANDO = porTexto("Continuar editando");
    private static final By BOTAO_OK = porTexto("OK");

    public TelaEdicaoTarefa aguardarCarregamento() {
        esperarVisivel(CAMPO_TITULO);
        return this;
    }

    public boolean estaAberta() {
        return estaVisivel(CAMPO_TITULO);
    }

    public String obterTitulo() {
        return obterTexto(CAMPO_TITULO);
    }

    public String obterTextoVencimento() {
        return esperarVisivel(LINHA_VENCIMENTO).findElement(AppiumBy.className("android.widget.TextView")).getText();
    }

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

    public TelaEdicaoTarefa informarTitulo(String titulo) {
        digitar(CAMPO_TITULO, titulo);
        return this;
    }

    public TelaEdicaoTarefa definirVencimentoHoje() {
        esconderTeclado();
        clicar(LINHA_VENCIMENTO);
        new SeletorData().aguardarAbertura().selecionarHoje().confirmar();
        return this;
    }

    public TelaEdicaoTarefa definirVencimento(LocalDate data) {
        esconderTeclado();
        clicar(LINHA_VENCIMENTO);
        new SeletorData().aguardarAbertura().selecionarData(data).confirmar();
        return this;
    }

    public TelaEdicaoTarefa definirPrioridade(Prioridade prioridade) {
        esconderTeclado();
        esperarVisivel(BOTOES_PRIORIDADE);
        buscarTodos(BOTOES_PRIORIDADE).get(prioridade.posicao()).click();
        return this;
    }

    public TelaEdicaoTarefa selecionarLista(String nomeLista) {
        esconderTeclado();
        clicar(SELETOR_LISTA);
        new SeletorLista().aguardarAbertura().selecionar(nomeLista);
        return this;
    }

    public TelaEdicaoTarefa adicionarSubtarefa(String titulo) {
        esconderTeclado();
        clicar(ADICIONAR_SUBTAREFA);
        esperarVisivel(CAMPO_SUBTAREFA_VAZIO).sendKeys(titulo);
        return this;
    }

    public TelaListaTarefas salvar() {
        esconderTeclado();
        clicar(BOTAO_SALVAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }

    public void tocarEmSalvar() {
        esconderTeclado();
        clicar(BOTAO_SALVAR);
    }

    public TelaEdicaoTarefa voltarDaEdicao() {
        voltar();
        return this;
    }

    public TelaListaTarefas descartarAlteracoes() {
        clicar(BOTAO_DESCARTAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }

    public TelaEdicaoTarefa continuarEditando() {
        clicar(BOTAO_CONTINUAR_EDITANDO);
        return aguardarCarregamento();
    }

    public TelaListaTarefas excluir() {
        esconderTeclado();
        clicar(BOTAO_EXCLUIR);
        clicar(BOTAO_OK);
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
