package support;

import pages.TelaEdicaoTarefa;
import pages.TelaListaTarefas;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Estado compartilhado entre as classes de passos durante um único cenário.
 * <p>
 * O PicoContainer cria uma instância nova a cada cenário e a injeta no construtor de todas as
 * classes de passos e de hooks que a declaram. Assim, um passo {@code Dado} pode abrir uma tela e
 * o passo {@code Então} seguinte continuar a partir dela, sem variáveis estáticas.
 * <p>
 * Guarda também o que o cenário alterou no aplicativo (tarefas criadas e a exibição das
 * concluídas), para que a limpeza ao final deixe o app como estava, mesmo quando o cenário falha.
 */
public class ContextoCenario {

    private TelaListaTarefas lista;
    private TelaEdicaoTarefa edicao;
    private final Set<String> tarefasCriadas = new LinkedHashSet<>();
    private boolean exibicaoConcluidasAlterada;

    /** Lista de tarefas aberta no momento. O hook {@code @Before} a deixa em "Minhas tarefas". */
    public TelaListaTarefas lista() {
        if (lista == null) {
            throw new IllegalStateException("A lista de tarefas ainda não foi aberta neste cenário.");
        }
        return lista;
    }

    public void definirLista(TelaListaTarefas lista) {
        this.lista = lista;
    }

    /** Tela de edição aberta no momento, quando o cenário está editando uma tarefa. */
    public TelaEdicaoTarefa edicao() {
        if (edicao == null) {
            throw new IllegalStateException("Nenhuma tarefa está em edição neste cenário.");
        }
        return edicao;
    }

    public void definirEdicao(TelaEdicaoTarefa edicao) {
        this.edicao = edicao;
    }

    // ---------- Massa de dados ----------

    /** Registra uma tarefa criada pelo cenário, para ser excluída na limpeza. */
    public void registrarTarefaCriada(String titulo) {
        tarefasCriadas.add(titulo);
    }

    /** Atualiza o registro quando o cenário salva um novo título para a tarefa. */
    public void registrarTarefaRenomeada(String tituloAnterior, String novoTitulo) {
        tarefasCriadas.remove(tituloAnterior);
        tarefasCriadas.add(novoTitulo);
    }

    /** Remove do registro a tarefa que o próprio cenário já excluiu. */
    public void registrarTarefaExcluida(String titulo) {
        tarefasCriadas.remove(titulo);
    }

    /** Tarefas criadas e ainda não excluídas, da mais recente para a mais antiga. */
    public List<String> tarefasParaLimpar() {
        List<String> tarefas = new ArrayList<>(tarefasCriadas);
        return tarefas.reversed();
    }

    // ---------- Preferências do aplicativo ----------

    /** Indica que o cenário ocultou as tarefas concluídas e a limpeza deve voltar a exibi-las. */
    public void registrarExibicaoConcluidasAlterada() {
        exibicaoConcluidasAlterada = true;
    }

    public boolean exibicaoConcluidasAlterada() {
        return exibicaoConcluidasAlterada;
    }
}
