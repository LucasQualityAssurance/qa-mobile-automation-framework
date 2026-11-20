package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Diálogo de escolha da lista à qual a tarefa pertence. */
public class SeletorLista extends BasePage {

    private static final By DIALOGO = AppiumBy.id("list_picker");

    public SeletorLista aguardarAbertura() {
        esperarVisivel(DIALOGO);
        return this;
    }

    @Step("Escolher a lista \"{nomeLista}\"")
    public void selecionar(String nomeLista) {
        clicar(porTexto(nomeLista));
        esperarDesaparecer(DIALOGO);
    }
}
