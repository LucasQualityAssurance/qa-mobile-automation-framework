package pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/** Tela exibida no primeiro acesso ao aplicativo. */
public class TelaBoasVindas extends BasePage {

    private static final By CONTINUAR_SEM_SINCRONIZAR = AppiumBy.id("continue_without_sync");

    public TelaListaTarefas continuarSemSincronizar() {
        clicar(CONTINUAR_SEM_SINCRONIZAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
