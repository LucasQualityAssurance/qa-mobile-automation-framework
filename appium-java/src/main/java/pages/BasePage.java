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
 * Classe base dos Page Objects: concentra esperas explícitas e ações comuns.
 * Nenhuma pausa fixa é utilizada; toda sincronização é feita por condição.
 */
public abstract class BasePage {

    private static final Duration INTERVALO_ESTABILIDADE = Duration.ofMillis(250);

    protected final AndroidDriver driver;
    private final WebDriverWait espera;

    protected BasePage() {
        this.driver = GerenciadorDriver.obterDriver();
        this.espera = new WebDriverWait(driver, Configuracao.esperaPadrao());
        this.espera.ignoring(StaleElementReferenceException.class);
    }

    // ---------- Localizadores utilitários ----------

    protected static By porTexto(String texto) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + texto + "\")");
    }

    protected static By porTextoContendo(String trecho) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(\"" + trecho + "\")");
    }

    protected static By porDescricao(String descricao) {
        return AppiumBy.accessibilityId(descricao);
    }

    // ---------- Esperas ----------

    protected WebElement esperarVisivel(By localizador) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    protected WebElement esperarClicavel(By localizador) {
        return espera.until(ExpectedConditions.elementToBeClickable(localizador));
    }

    protected void esperarDesaparecer(By localizador) {
        espera.until(ExpectedConditions.invisibilityOfElementLocated(localizador));
    }

    /**
     * Aguarda o elemento parar de se mover (fim de animações de abertura), comparando
     * a posição dele entre duas leituras consecutivas.
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

    protected boolean estaVisivel(By localizador) {
        return estaVisivel(localizador, Configuracao.esperaCurta());
    }

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

    protected List<WebElement> buscarTodos(By localizador) {
        return driver.findElements(localizador);
    }

    // ---------- Ações ----------

    protected void clicar(By localizador) {
        esperarClicavel(localizador).click();
    }

    protected void digitar(By localizador, String texto) {
        WebElement campo = esperarVisivel(localizador);
        campo.clear();
        campo.sendKeys(texto);
    }

    protected String obterTexto(By localizador) {
        return esperarVisivel(localizador).getText();
    }

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
