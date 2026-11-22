package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

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

    /** Rola até "Salvar", toca nele e espera a tela fechar. */
    @Step("Salvar a lista")
    public void salvar() {
        // O botão "Salvar" fica no fim da tela e exige rolagem
        rolarAteTexto("Salvar").click();
        esperarDesaparecer(CAMPO_NOME);
    }
}
