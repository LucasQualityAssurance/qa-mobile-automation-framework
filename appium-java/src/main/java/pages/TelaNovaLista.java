package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Tela de criação de uma lista local. */
public class TelaNovaLista extends BasePage {

    private static final By CAMPO_NOME = AppiumBy.className("android.widget.EditText");

    public TelaNovaLista aguardarCarregamento() {
        esperarVisivel(CAMPO_NOME);
        return this;
    }

    @Step("Informar o nome da lista \"{nome}\"")
    public TelaNovaLista informarNome(String nome) {
        digitar(CAMPO_NOME, nome);
        esconderTeclado();
        return this;
    }

    @Step("Salvar a lista")
    public void salvar() {
        // O botão "Salvar" fica no fim da tela e exige rolagem
        rolarAteTexto("Salvar").click();
        esperarDesaparecer(CAMPO_NOME);
    }
}
