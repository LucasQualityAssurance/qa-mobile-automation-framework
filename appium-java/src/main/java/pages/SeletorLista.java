package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Diálogo de escolha da lista à qual a tarefa pertence.
 * <p>
 * <b>Quando aparece:</b> ao tocar na linha da lista na {@link TelaEdicaoTarefa}.
 * <p>
 * <b>O que tem na tela:</b> as listas disponíveis ("Lista padrão" e as criadas pelo usuário).
 * Tocar em uma delas já confirma a escolha; não há botão "OK".
 * <p>
 * <b>Para onde vai:</b> o diálogo fecha e a tela de edição continua aberta.
 */
public class SeletorLista extends BasePage {

    private static final By DIALOGO = AppiumBy.id("list_picker");

    public SeletorLista aguardarAbertura() {
        esperarVisivel(DIALOGO);
        return this;
    }

    /** Toca na lista pelo nome e espera o diálogo fechar. */
    @Step("Escolher a lista \"{nomeLista}\"")
    public void selecionar(String nomeLista) {
        clicar(porTexto(nomeLista));
        esperarDesaparecer(DIALOGO);
    }
}
