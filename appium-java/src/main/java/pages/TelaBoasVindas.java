package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Tela exibida no primeiro acesso ao aplicativo. */
public class TelaBoasVindas extends BasePage {

    private static final By CONTINUAR_SEM_SINCRONIZAR = AppiumBy.id("continue_without_sync");

    @Step("Continuar sem sincronizar")
    public TelaListaTarefas continuarSemSincronizar() {
        clicar(CONTINUAR_SEM_SINCRONIZAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
