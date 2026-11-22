package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Menu lateral (navigation drawer), com os filtros e as listas do usuário.
 * <p>
 * <b>Quando aparece:</b> ao tocar no ícone de menu da barra inferior da {@link TelaListaTarefas}.
 * <p>
 * <b>O que tem na tela</b>, de cima para baixo:
 * <ul>
 *   <li>"Minhas tarefas" (todas as tarefas);</li>
 *   <li>seção "Filtros": "Hoje", "Modificadas recentemente"...;</li>
 *   <li>seções "Etiquetas" e "Lugares";</li>
 *   <li>seção "Listas locais", com "Lista padrão", as listas criadas pelo usuário e o botão
 *       "Criar nova lista" (ícone "+" ao lado do título da seção).</li>
 * </ul>
 * <b>Comportamentos importantes:</b>
 * <ul>
 *   <li>O menu é feito em Jetpack Compose e ocupa a tela inteira. Os itens têm apenas texto,
 *       sem id próprio, por isso são buscados pelo texto dentro do menu.</li>
 *   <li>Ele abre com animação. Tocar num item antes de a animação terminar faz o toque cair em
 *       outro item (por exemplo, no cabeçalho "Filtros", que só recolhe a seção). Por isso os
 *       métodos esperam a posição dos itens se estabilizar antes de tocar.</li>
 * </ul>
 * <b>Para onde vai:</b> {@link TelaListaTarefas} filtrada pelo item escolhido, ou
 * {@link TelaNovaLista} ao criar uma lista.
 */
public class MenuLateral extends BasePage {

    private static final By MENU = AppiumBy.id("navigation_drawer");
    private static final By CRIAR_NOVA_LISTA = porDescricao("Criar nova lista");
    /** Primeiro item do menu, usado para saber quando a animação de abertura terminou. */
    private static final By PRIMEIRO_ITEM = itemDoMenu("Minhas tarefas");

    /** Item dentro do menu, sem confundir com textos iguais da tela por trás (ex.: título da lista). */
    private static By itemDoMenu(String texto) {
        return AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"navigation_drawer\")"
                + ".childSelector(new UiSelector().text(" + literalUiSelector(texto) + "))");
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

    /** {@code true} quando o menu mostra um item com o texto informado (filtro ou lista). */
    public boolean exibeOpcao(String opcao) {
        return estaVisivel(itemDoMenu(opcao));
    }

    /**
     * Toca no filtro ou na lista pelo nome exibido, como "Hoje" ou "Projetos de TI".
     * O menu fecha e a lista de tarefas passa a mostrar o item escolhido.
     */
    @Step("Selecionar \"{opcao}\" no menu lateral")
    public TelaListaTarefas selecionar(String opcao) {
        esperarPosicaoEstavel(itemDoMenu(opcao)).click();
        return new TelaListaTarefas().aguardarCarregamento();
    }

    /**
     * Cria uma lista local pelo "+" da seção "Listas locais" e espera a volta para a lista de
     * tarefas. A lista nova passa a aparecer no menu e no {@link SeletorLista}.
     */
    @Step("Criar a lista \"{nome}\" pelo menu lateral")
    public TelaListaTarefas criarLista(String nome) {
        clicar(CRIAR_NOVA_LISTA);
        new TelaNovaLista().aguardarCarregamento().informarNome(nome).salvar();
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
