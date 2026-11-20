package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Painel inferior de ordenação e exibição da lista. */
public class PainelOrdenacao extends BasePage {

    public static final String ORDEM_POR_TITULO = "Por título";
    public static final String ORDEM_POR_PRIORIDADE = "Por prioridade";

    private static final By OPCAO_LISTAGEM = porTexto("Listagem");
    private static final By INTERRUPTOR_MOSTRAR_CONCLUIDAS = By.xpath(
            "//android.widget.TextView[@text='Mostrar tarefas concluídas']/following-sibling::android.view.View");

    public PainelOrdenacao aguardarAbertura() {
        esperarVisivel(OPCAO_LISTAGEM);
        return this;
    }

    public TelaListaTarefas ordenarPor(String ordem) {
        clicar(OPCAO_LISTAGEM);
        clicar(porTexto(ordem));
        return fechar();
    }

    public PainelOrdenacao definirMostrarConcluidas(boolean exibir) {
        WebElement interruptor = esperarVisivel(INTERRUPTOR_MOSTRAR_CONCLUIDAS);
        if (Boolean.parseBoolean(interruptor.getAttribute("checked")) != exibir) {
            interruptor.click();
        }
        return this;
    }

    public TelaListaTarefas fechar() {
        if (estaVisivel(OPCAO_LISTAGEM)) {
            voltar();
            esperarDesaparecer(OPCAO_LISTAGEM);
        }
        return new TelaListaTarefas().aguardarCarregamento();
    }
}
