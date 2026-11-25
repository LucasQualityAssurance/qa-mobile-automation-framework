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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;

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
 * <p>
 * <b>Logs:</b> toda ação (tocar, digitar, voltar, rolar) é registrada em INFO com o nome da tela,
 * e toda espera em DEBUG com o localizador. O INFO aparece no terminal e no Allure; o DEBUG só no
 * Allure ("Log da etapa"). Quando uma espera estoura o tempo, o localizador é registrado em WARN.
 */
public abstract class BasePage {

    /** Intervalo entre as leituras de posição usadas para detectar o fim de uma animação. */
    private static final Duration INTERVALO_ESTABILIDADE = Duration.ofMillis(250);

    /** Logger com o nome da tela concreta: aparece como [TelaListaTarefas], [MenuLateral]... */
    protected final Logger log = LoggerFactory.getLogger(getClass());
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
        return AppiumBy.androidUIAutomator("new UiSelector().text(" + literalUiSelector(texto) + ")");
    }

    /** Elemento cujo texto contém o trecho informado. */
    protected static By porTextoContendo(String trecho) {
        return AppiumBy.androidUIAutomator("new UiSelector().textContains(" + literalUiSelector(trecho) + ")");
    }

    /**
     * Monta um literal de texto seguro para expressões {@code UiSelector}, escapando barras e aspas.
     * Sem o escape, um texto com aspas quebraria o localizador.
     */
    protected static String literalUiSelector(String texto) {
        return "\"" + texto.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** Elemento pela descrição de acessibilidade ({@code content-desc}), comum em botões só com ícone. */
    protected static By porDescricao(String descricao) {
        return AppiumBy.accessibilityId(descricao);
    }

    // ---------- Esperas ----------

    /** Espera o elemento aparecer na tela e o devolve. Falha após o tempo padrão. */
    protected WebElement esperarVisivel(By localizador) {
        return aguardar("ficar visível", localizador, ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    /** Espera o elemento estar visível e habilitado para receber toque. */
    protected WebElement esperarClicavel(By localizador) {
        return aguardar("ficar clicável", localizador, ExpectedConditions.elementToBeClickable(localizador));
    }

    /** Espera o elemento sair da tela (ex.: um diálogo que foi fechado). */
    protected void esperarDesaparecer(By localizador) {
        aguardar("desaparecer", localizador, ExpectedConditions.invisibilityOfElementLocated(localizador));
    }

    /**
     * Executa a espera padrão registrando no log o que está sendo aguardado. Se o tempo acabar,
     * registra em WARN qual localizador falhou antes de propagar o erro.
     */
    private <T> T aguardar(String condicao, By localizador, ExpectedCondition<T> esperado) {
        log.debug("Aguardando {}: {}", condicao, descrever(localizador));
        try {
            return espera.until(esperado);
        } catch (TimeoutException e) {
            log.warn("Tempo esgotado ({} s) aguardando {}: {}",
                    Configuracao.esperaPadrao().toSeconds(), condicao, descrever(localizador));
            throw e;
        }
    }

    /** Texto legível do localizador para os logs, sem o prefixo "By.". */
    protected static String descrever(By localizador) {
        String texto = localizador.toString();
        return texto.startsWith("By.") ? texto.substring(3) : texto;
    }

    /**
     * Aguarda o elemento parar de se mover (fim de animações de abertura), comparando
     * a posição dele entre duas leituras consecutivas.
     * <p>
     * Use antes de tocar em itens de painéis que deslizam, como o menu lateral: tocar durante a
     * animação faz o toque cair em outro item.
     */
    protected WebElement esperarPosicaoEstavel(By localizador) {
        log.debug("Aguardando parar de se mover: {}", descrever(localizador));
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
        boolean visivel;
        try {
            new WebDriverWait(driver, tempo)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.visibilityOfElementLocated(localizador));
            visivel = true;
        } catch (TimeoutException e) {
            visivel = false;
        }
        log.debug("Consulta \"está visível\" ({} s): {} -> {}", tempo.toSeconds(), descrever(localizador), visivel);
        return visivel;
    }

    /**
     * Verifica se o elemento sai da tela (ou nunca aparece) dentro do tempo padrão.
     * Não falha: devolve {@code false} quando o elemento continua visível. Use nas validações
     * de ausência, que precisam aguardar animações e atualizações da lista.
     */
    protected boolean estaAusente(By localizador) {
        try {
            esperarDesaparecer(localizador);
            log.debug("Consulta \"está ausente\": {} -> true", descrever(localizador));
            return true;
        } catch (TimeoutException e) {
            log.debug("Consulta \"está ausente\": {} -> false", descrever(localizador));
            return false;
        }
    }

    /**
     * Igual ao {@link #estaAusente(By)}, com tempo de espera informado e sem registrar WARN ao
     * esgotar o tempo: use quando "ainda visível" é um resultado esperado que leva a outra ação.
     */
    protected boolean estaAusente(By localizador, Duration tempo) {
        boolean ausente;
        try {
            new WebDriverWait(driver, tempo)
                    .ignoring(StaleElementReferenceException.class)
                    .until(ExpectedConditions.invisibilityOfElementLocated(localizador));
            ausente = true;
        } catch (TimeoutException e) {
            ausente = false;
        }
        log.debug("Consulta \"está ausente\" ({} s): {} -> {}", tempo.toSeconds(), descrever(localizador), ausente);
        return ausente;
    }

    /**
     * Aguarda uma condição qualquer se tornar verdadeira, repetindo a verificação até o tempo
     * informado. Não falha: devolve {@code false} quando o tempo acaba.
     */
    protected boolean aguardarCondicao(BooleanSupplier condicao, Duration tempo) {
        try {
            new WebDriverWait(driver, tempo)
                    .ignoring(StaleElementReferenceException.class)
                    .ignoring(NoSuchElementException.class)
                    .until(d -> condicao.getAsBoolean());
            return true;
        } catch (TimeoutException e) {
            log.debug("Condição não atendida em {} s", tempo.toSeconds());
            return false;
        }
    }

    /** Busca imediata (sem espera) de todos os elementos que atendem ao localizador. */
    protected List<WebElement> buscarTodos(By localizador) {
        List<WebElement> elementos = driver.findElements(localizador);
        log.debug("Busca imediata: {} -> {} elemento(s)", descrever(localizador), elementos.size());
        return elementos;
    }

    // ---------- Ações ----------

    /** Espera o elemento ficar clicável e toca nele. */
    protected void clicar(By localizador) {
        log.info("Tocar: {}", descrever(localizador));
        esperarClicavel(localizador).click();
    }

    /**
     * Toca no botão e confirma que o app foi para a tela seguinte. Se a tela seguinte não
     * aparecer no tempo curto e o botão continuar na tela, o toque se perdeu (comum logo após a
     * tela abrir, enquanto o app ainda carrega) e é repetido uma única vez.
     * <p>
     * Não espera a tela seguinte até o fim: quem chama continua esperando por ela normalmente.
     */
    protected void clicarConfirmandoSaida(By botao, By telaSeguinte) {
        clicar(botao);
        if (estaVisivel(telaSeguinte, Configuracao.esperaCurta())) {
            return;
        }
        if (estaVisivel(botao, Duration.ofSeconds(1))) {
            log.warn("O toque em {} não teve efeito: tocando de novo", descrever(botao));
            clicar(botao);
        }
    }

    /** Limpa o campo e digita o texto informado. */
    protected void digitar(By localizador, String texto) {
        log.info("Digitar \"{}\" em: {}", texto, descrever(localizador));
        WebElement campo = esperarVisivel(localizador);
        campo.clear();
        campo.sendKeys(texto);
    }

    /** Espera o elemento aparecer e devolve o texto dele. */
    protected String obterTexto(By localizador) {
        String texto = esperarVisivel(localizador).getText();
        log.debug("Texto lido de {}: \"{}\"", descrever(localizador), texto);
        return texto;
    }

    /** Fecha o teclado virtual quando ele está aberto, para não cobrir botões da tela. */
    protected void esconderTeclado() {
        if (driver.isKeyboardShown()) {
            log.info("Fechar o teclado virtual");
            driver.hideKeyboard();
        }
    }

    /** Pressiona o botão "voltar" do Android, fechando o teclado antes se estiver aberto. */
    protected void voltar() {
        esconderTeclado();
        log.info("Pressionar o botão \"voltar\" do Android");
        driver.navigate().back();
    }

    /** Rola a primeira área rolável da tela até encontrar o texto informado e o retorna. */
    protected WebElement rolarAteTexto(String texto) {
        log.info("Rolar a tela até o texto \"{}\"", texto);
        return esperarVisivel(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true))"
                        + ".scrollIntoView(new UiSelector().text(" + literalUiSelector(texto) + "))"));
    }
}
