package passos;

import io.cucumber.java.pt.Quando;
import net.datafaker.Faker;
import suporte.ContextoCenario;

/**
 * Passos de busca e de ordenação da lista.
 * <p>
 * A busca fica aberta depois do passo {@code Quando}; o passo {@code Então} correspondente valida
 * o resultado e fecha a busca. Se a validação falhar, o hook de limpeza ({@code @After}) fecha a
 * busca pelo caminho de recuperação da {@code NavegacaoApp}.
 */
public class PassosBuscaOrdenacao {

    private final ContextoCenario contexto;

    public PassosBuscaOrdenacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("eu busco por {string}")
    public void euBuscoPor(String termo) {
        contexto.lista().buscar(termo);
    }

    /** Termo aleatório de 12 letras: não coincide com nenhum título da massa. */
    @Quando("eu busco por um termo inexistente")
    public void euBuscoPorUmTermoInexistente() {
        contexto.lista().buscar(new Faker().regexify("[a-z]{12}"));
    }

    /** Recebe o texto da opção como aparece no app, por exemplo "Por título" ou "Por prioridade". */
    @Quando("eu ordeno a lista {string}")
    public void euOrdenoALista(String ordem) {
        contexto.definirLista(contexto.lista().abrirOrdenacao().ordenarPor(ordem));
    }
}
