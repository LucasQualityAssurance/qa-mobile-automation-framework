package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Menu lateral (navigation drawer) com filtros e listas. */
public class MenuLateral extends BasePage {

    private static final By MENU = AppiumBy.id("navigation_drawer");
    private static final By CRIAR_NOVA_LISTA = porDescricao("Criar nova lista");
    private static final By PRIMEIRO_ITEM = itemDoMenu("Minhas tarefas");

    /** Item dentro do menu, sem confundir com textos iguais da tela por trás (ex.: título da lista). */
    private static By itemDoMenu(String texto) {
        return AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"navigation_drawer\")"
                + ".childSelector(new UiSelector().text(\"" + texto + "\"))");
    }

    /** O menu abre com animação: aguarda o primeiro item parar de se mover antes de interagir. */
    public MenuLateral aguardarAbertura() {
        esperarVisivel(MENU);
        esperarPosicaoEstavel(PRIMEIRO_ITEM);
        return this;
    }

    public boolean estaAberto() {
        return estaVisivel(MENU);
    }

    public boolean exibeOpcao(String opcao) {
        return estaVisivel(itemDoMenu(opcao));
    }

    @Step("Selecionar \"{opcao}\" no menu lateral")
    public TelaListaTarefas selecionar(String opcao) {
        esperarPosicaoEstavel(itemDoMenu(opcao)).click();
        return new TelaListaTarefas().aguardarCarregamento();
    }

    @Step("Criar a lista \"{nome}\" pelo menu lateral")
    public TelaListaTarefas criarLista(String nome) {
        clicar(CRIAR_NOVA_LISTA);
        new TelaNovaLista().aguardarCarregamento().informarNome(nome).salvar();
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
