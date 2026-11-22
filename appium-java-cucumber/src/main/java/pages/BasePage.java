package pages;

import config.Configuracao;
import driver.GerenciadorDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Classe base de todos os Page Objects: concentra as esperas explícitas e as ações comuns.
 * <p>
 * Regras que valem para o projeto inteiro:
 * <ul>
 *   <li>Nenhuma pausa fixa ({@code Thread.sleep}) é usada. Toda sincronização espera uma
 *       condição: elemento visível, clicável, desaparecido ou parado.</li>
 *   <li>A espera implícita do driver é zero (ver {@code GerenciadorDriver}). Por isso toda busca
 *       deve passar pelos métodos de espera desta classe.</li>
 *   <li>O tempo máximo de espera vem de {@code tempo.espera.padrao} no {@code config.properties}.</li>
 * </ul>
 * Ao criar uma nova tela, estenda esta classe e use apenas os métodos {@code protected} abaixo.
 */
public abstract class BasePage {

    /** Intervalo entre as leituras de posição usadas para detectar o fim de uma animação. */
    private static final Duration INTERVALO_ESTABILIDADE = Duration.ofMillis(250);

    protected final AndroidDriver driver;
    private final WebDriverWait espera;

    protected BasePage() {
        this.driver = GerenciadorDriver.obterDriver();
        this.espera = new WebDriverWait(driver, Configuracao.esperaPadrao());
        // Listas do Android são redesenhadas com frequência: um elemento encontrado pode ficar
        // "velho" (stale) logo em seguida. Nesse caso a espera simplesmente tenta de novo.
        this.espera.ignoring(StaleElementReferenceException.class);
    }

    // ---------- Localizadores utilitários ----------

    /** Elemento cujo texto é exatamente igual ao informado (ex.: um botão "Salvar"). */
    protected static By porTexto(String texto) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + texto + "\")");
    }

    /** Elemento cujo texto contém o trecho informado. */
    protected static By porTextoContendo(String trecho) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + trecho + "\")");
    }

    /** Elemento pela descrição de acessibilidade ({@code content-desc}), comum em botões só com ícone. */
    protected static By porDescricao(String descricao) {
        return AppiumBy.accessibilityId(descricao);
    }

    // ---------- Esperas ----------

    /** Espera o elemento aparecer na tela e o devolve. Falha após o tempo padrão. */
    protected WebElement esperarVisivel(By localizador) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    /** Espera o elemento estar visível e habilitado para receber toque. */
    protected WebElement esperarClicavel(By localizador) {
        return espera.until(ExpectedConditions.elementToBeClickable(localizador));
    }

    /** Espera o elemento sair da tela (ex.: um diálogo que foi fechado). */
    protected void esperarDesaparecer(By localizador) {
        espera.until(ExpectedConditions.invisibilityOfElementLocated(localizador));
    }

    /**
     * Aguarda o elemento parar de se mover (fim de animações de abertura), comparando
     * a posição dele entre duas leituras consecutivas.
     * <p>
     * Use antes de tocar em itens de painéis que deslizam, como o menu lateral: tocar durante a
     * animação faz o toque cair em outro item.
     */
    protected WebElement esperarPosicaoEstavel(By localizador) {
        Rectangle[] anterior = new Rectangle[1];
        return new WebDriverWait(driver, Configuracao.esperaPadrao(), INTERVALO_ESTABILIDADE)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .until(d -> {
                    WebElement elemento = d.findElement(localizador);
                    Rectangle atual = elemento.getRect();
                    boolean estavel = atual.equals(anterior[0]);
                    anterior[0] = atual;
                    return estavel ? elemento : null;
                });
    }

    /**
     * Verifica se o elemento aparece dentro do tempo curto ({@code tempo.espera.curta}).
     * Não falha: devolve {@code false} quando o elemento não aparece. Use em consultas e decisões.
     */
    protected boolean estaVisivel(By localizador) {
        return estaVisivel(localizador, Configuracao.esperaCurta());
    }

    /** Igual ao {@link #estaVisivel(By)}, com tempo de espera informado. */
    protected boolean estaVisivel(By localizador, Duration tempo) {
        try {
            new WebDriverWait(driver, tempo)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.visibilityOfElementLocated(localizador));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Busca imediata (sem espera) de todos os elementos que atendem ao localizador. */
    protected List<WebElement> buscarTodos(By localizador) {
        return driver.findElements(localizador);
    }

    // ---------- Ações ----------

    /** Espera o elemento ficar clicável e toca nele. */
    protected void clicar(By localizador) {
        esperarClicavel(localizador).click();
    }

    /** Limpa o campo e digita o texto informado. */
    protected void digitar(By localizador, String texto) {
        WebElement campo = esperarVisivel(localizador);
        campo.clear();
        campo.sendKeys(texto);
    }

    /** Espera o elemento aparecer e devolve o texto dele. */
    protected String obterTexto(By localizador) {
        return esperarVisivel(localizador).getText();
    }

    /** Fecha o teclado virtual quando ele está aberto, para não cobrir botões da tela. */
    protected void esconderTeclado() {
        if (driver.isKeyboardShown()) {
            driver.hideKeyboard();
        }
    }

    /** Pressiona o botão "voltar" do Android, fechando o teclado antes se estiver aberto. */
    protected void voltar() {
        esconderTeclado();
        driver.navigate().back();
    }

    /** Rola a primeira área rolável da tela até encontrar o texto informado e o retorna. */
    protected WebElement rolarAteTexto(String texto) {
        return esperarVisivel(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().text(\"" + texto + "\"))"));
    }
}
