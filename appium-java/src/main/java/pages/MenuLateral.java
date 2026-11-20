package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/** Menu lateral (navigation drawer) com filtros e listas. */
public class MenuLateral extends BasePage {

    private static final By MENU = AppiumBy.id("navigation_drawer");
    private static final By CRIAR_NOVA_LISTA = porDescricao("Criar nova lista");

    public MenuLateral aguardarAbertura() {
        esperarVisivel(MENU);
        return this;
    }

    public boolean estaAberto() {
        return estaVisivel(MENU);
    }

    public boolean exibeOpcao(String opcao) {
        return estaVisivel(porTexto(opcao));
    }

    @Step("Selecionar \"{opcao}\" no menu lateral")
    public TelaListaTarefas selecionar(String opcao) {
        clicar(porTexto(opcao));
        return new TelaListaTarefas().aguardarCarregamento();
    }

    @Step("Criar a lista \"{nome}\" pelo menu lateral")
    public TelaListaTarefas criarLista(String nome) {
        clicar(CRIAR_NOVA_LISTA);
        new TelaNovaLista().aguardarCarregamento().informarNome(nome).salvar();
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
