package pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Painel inferior de ordenação e exibição da lista.
 * <p>
 * <b>Quando aparece:</b> ao tocar no ícone de ordenação da barra inferior da {@link TelaListaTarefas}.
 * <p>
 * <b>O que tem na tela:</b>
 * <ul>
 *   <li>"Listagem", que abre as opções de ordem, entre elas "Por título" e "Por prioridade";</li>
 *   <li>o interruptor "Mostrar tarefas concluídas";</li>
 *   <li>outras opções de agrupamento e exibição, que a automação não usa.</li>
 * </ul>
 * <b>Para onde vai:</b> fecha com "voltar" e devolve a {@link TelaListaTarefas} já reorganizada.
 * As escolhas ficam gravadas no app e continuam valendo nos testes seguintes.
 */
public class PainelOrdenacao extends BasePage {

    /** Textos exatos das opções de ordem, para os testes não repetirem strings soltas. */
    public static final String ORDEM_POR_TITULO = "Por título";
    public static final String ORDEM_POR_PRIORIDADE = "Por prioridade";

    private static final By OPCAO_LISTAGEM = porTexto("Listagem");
    /** O interruptor não tem id: é o elemento ao lado do texto "Mostrar tarefas concluídas". */
    private static final By INTERRUPTOR_MOSTRAR_CONCLUIDAS = By.xpath(
            "//android.widget.TextView[@text='Mostrar tarefas concluídas']/following-sibling::android.view.View");

    public PainelOrdenacao aguardarAbertura() {
        esperarVisivel(OPCAO_LISTAGEM);
        return this;
    }

    /** Escolhe a ordem em "Listagem" e fecha o painel. Use as constantes {@code ORDEM_*}. */
    @Step("Ordenar a listagem \"{ordem}\"")
    public TelaListaTarefas ordenarPor(String ordem) {
        clicar(OPCAO_LISTAGEM);
        clicar(porTexto(ordem));
        return fechar();
    }

    /** Liga ou desliga "Mostrar tarefas concluídas"; só toca quando o estado atual é diferente. */
    @Step("Definir \"Mostrar tarefas concluídas\" como {exibir}")
    public PainelOrdenacao definirMostrarConcluidas(boolean exibir) {
        WebElement interruptor = esperarVisivel(INTERRUPTOR_MOSTRAR_CONCLUIDAS);
        if (Boolean.parseBoolean(interruptor.getAttribute("checked")) != exibir) {
            interruptor.click();
        }
        return this;
    }

    /** Fecha o painel, caso ainda esteja aberto, e espera a lista. */
    @Step("Fechar o painel de ordenação")
    public TelaListaTarefas fechar() {
        if (estaVisivel(OPCAO_LISTAGEM)) {
            voltar();
            esperarDesaparecer(OPCAO_LISTAGEM);
        }
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
