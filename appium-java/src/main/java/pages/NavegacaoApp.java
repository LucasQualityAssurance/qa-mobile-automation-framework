package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

/**
 * Leva o aplicativo, a partir de qualquer tela, até a lista "Minhas tarefas"
 * sem reiniciá-lo. Usado entre os testes para manter o app sempre aberto.
 */
public class NavegacaoApp extends BasePage {

    private static final Duration VERIFICACAO_RAPIDA = Duration.ofSeconds(1);
    private static final int LIMITE_TENTATIVAS = 6;

    private static final By CONTINUAR_SEM_SINCRONIZAR = AppiumBy.id("continue_without_sync");
    private static final By BOTAO_NOVA_TAREFA = AppiumBy.id("org.tasks:id/fab");
    private static final By CAMPO_BUSCA = AppiumBy.id("org.tasks:id/search_src_text");
    private static final By MENU_LATERAL = AppiumBy.id("navigation_drawer");
    private static final By BOTAO_DESCARTAR = porTexto("Descartar");
    private static final By TITULO_TELA = By.xpath(
            "//*[@resource-id='org.tasks:id/toolbar']//android.widget.TextView");

    @Step("Ir para a lista \"Minhas tarefas\"")
    public TelaListaTarefas irParaMinhasTarefas() {
        for (int tentativa = 0; tentativa < LIMITE_TENTATIVAS; tentativa++) {
            if (estaVisivel(CONTINUAR_SEM_SINCRONIZAR, VERIFICACAO_RAPIDA)) {
                return new TelaBoasVindas().continuarSemSincronizar();
            }
            if (estaVisivel(BOTAO_DESCARTAR, VERIFICACAO_RAPIDA)) {
                clicar(BOTAO_DESCARTAR);
                continue;
            }
            if (estaVisivel(CAMPO_BUSCA, VERIFICACAO_RAPIDA)) {
                new TelaListaTarefas().fecharBusca();
                continue;
            }
            boolean naLista = estaVisivel(BOTAO_NOVA_TAREFA, VERIFICACAO_RAPIDA)
                    && !estaVisivel(MENU_LATERAL, VERIFICACAO_RAPIDA);
            if (naLista) {
                TelaListaTarefas lista = new TelaListaTarefas();
                if (!"Minhas tarefas".equals(obterTexto(TITULO_TELA))) {
                    lista = lista.abrirMenuLateral().selecionar("Minhas tarefas");
                }
                return lista;
            }
            voltar();
        }
        throw new IllegalStateException("Não foi possível retornar à lista \"Minhas tarefas\".");
    }
}
