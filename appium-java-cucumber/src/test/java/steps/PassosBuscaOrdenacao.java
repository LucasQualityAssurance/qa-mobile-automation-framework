package steps;

import io.cucumber.java.pt.Quando;
import net.datafaker.Faker;
import support.ContextoCenario;

/**
 * Passos de busca e de ordenação da lista.
 * <p>
 * A busca fica aberta depois do passo {@code Quando}; o passo {@code Então} correspondente valida
 * o resultado e fecha a busca. Se a validação falhar, o hook de limpeza ({@code @After}) fecha a
 * busca pelo caminho de recuperação da {@code NavegacaoApp}.
 * <p>
 * Usados em {@code organizacao_lista.feature} e, no fim do ciclo de vida, para provar que uma
 * tarefa excluída não é mais encontrada.
 */
public class PassosBuscaOrdenacao {

    private final ContextoCenario contexto;

    /** O PicoContainer injeta o mesmo {@link ContextoCenario} em todas as classes de passos do cenário. */
    public PassosBuscaOrdenacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /**
     * Abre a busca e digita o termo. A lista passa a mostrar só as tarefas cujo título contém o
     * termo; a validação fica para o passo {@code Então}, que também fecha a busca.
     */
    @Quando("eu busco por {string}")
    public void euBuscoPor(String termo) {
        contexto.lista().buscar(termo);
    }

    /**
     * Busca por um termo aleatório de 12 letras (gerado pelo Datafaker). Como é sorteado a cada
     * execução, não coincide com nenhum título da massa nem de execuções anteriores.
     */
    @Quando("eu busco por um termo inexistente")
    public void euBuscoPorUmTermoInexistente() {
        contexto.lista().buscar(new Faker().regexify("[a-z]{12}"));
    }

    /**
     * Abre o painel de ordenação e escolhe a opção pelo texto exibido no app, por exemplo
     * "Por título" ou "Por prioridade". O painel fecha e a lista é reorganizada.
     * <p>
     * A ordenação fica gravada no app (como para qualquer pessoa usuária); os demais cenários não
     * dependem dela.
     */
    @Quando("eu ordeno a lista {string}")
    public void euOrdenoALista(String ordem) {
        contexto.definirLista(contexto.lista().abrirOrdenacao().ordenarPor(ordem));
    }
}
