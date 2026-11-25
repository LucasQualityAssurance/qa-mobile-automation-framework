package pages;

import config.Configuracao;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Tela de criação de uma lista local.
 * <p>
 * <b>Quando aparece:</b> ao tocar em "Criar nova lista" no {@link MenuLateral}.
 * <p>
 * <b>O que tem na tela:</b> o campo de nome, opções de personalização da lista e, no fim da
 * tela, o botão "Salvar", que só aparece depois de rolar.
 * <p>
 * <b>Para onde vai:</b> ao salvar, a tela fecha e o app volta para a {@link TelaListaTarefas}.
 */
public class TelaNovaLista extends BasePage {

    /** Campo de nome da lista; é o único campo de texto da tela. */
    private static final By CAMPO_NOME = AppiumBy.className("android.widget.EditText");
    private static final String TEXTO_SALVAR = "Salvar";

    public TelaNovaLista aguardarCarregamento() {
        esperarVisivel(CAMPO_NOME);
        return this;
    }

    /** Digita o nome e fecha o teclado, que cobriria o restante da tela. */
    @Step("Informar o nome da lista \"{nome}\"")
    public TelaNovaLista informarNome(String nome) {
        digitar(CAMPO_NOME, nome);
        esconderTeclado();
        return this;
    }

    /**
     * Rola até "Salvar", toca nele e espera a tela fechar.
     * <p>
     * Se a tela continuar aberta, o toque não chegou ao botão. Isso acontece quando o teclado
     * reabre por cima dele ou quando a tela ainda está deslizando por causa da rolagem. Nesse
     * caso, fecha o teclado e toca de novo, uma única vez.
     */
    @Step("Salvar a lista")
    public void salvar() {
        tocarEmSalvar();
        if (estaAusente(CAMPO_NOME, Configuracao.esperaCurta())) {
            return;
        }
        log.warn("A tela de nova lista continuou aberta após tocar em \"Salvar\": tocando de novo");
        tocarEmSalvar();
        esperarDesaparecer(CAMPO_NOME);
    }

    private void tocarEmSalvar() {
        // Teclado aberto cobre o fim da tela, onde fica o botão "Salvar"
        esconderTeclado();
        // O botão fica no fim da tela e exige rolagem
        rolarAteTexto(TEXTO_SALVAR);
        // Toca só depois que a rolagem termina; durante o deslize o toque cai em outro ponto
        WebElement botaoSalvar = esperarPosicaoEstavel(porTexto(TEXTO_SALVAR));
        log.info("Tocar em \"Salvar\"");
        botaoSalvar.click();
    }
}
