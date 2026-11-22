package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Tela de boas-vindas, exibida no primeiro acesso ao aplicativo.
 * <p>
 * <b>Quando aparece:</b> logo após a instalação ou depois que os dados do app são limpos.
 * Como a suíte limpa os dados no início ({@code app.limpar.dados.inicio.suite=true}), o primeiro
 * teste sempre passa por aqui.
 * <p>
 * <b>O que tem na tela:</b> opções de entrar com uma conta de sincronização e o botão
 * "Continuar sem sincronizar", que é o único usado pela automação.
 * <p>
 * <b>Para onde vai:</b> {@link TelaListaTarefas}, na lista "Minhas tarefas".
 */
public class TelaBoasVindas extends BasePage {

    /** Botão "Continuar sem sincronizar" (testTag do Compose, por isso sem o prefixo org.tasks:id/). */
    private static final By CONTINUAR_SEM_SINCRONIZAR = AppiumBy.id("continue_without_sync");

    /** Segue sem conta de sincronização e espera a lista de tarefas carregar. */
    @Step("Continuar sem sincronizar")
    public TelaListaTarefas continuarSemSincronizar() {
        clicar(CONTINUAR_SEM_SINCRONIZAR);
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
