package hooks;

import driver.GerenciadorDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.NavegacaoApp;
import pages.TelaListaTarefas;
import support.AllureAmbiente;
import support.ContextoCenario;

/**
 * Hooks do Cucumber: o que acontece antes e depois da execução, de cada cenário e de cada passo.
 * <ul>
 *   <li>{@code @BeforeAll}: abre uma única sessão Appium para toda a execução e registra o
 *       ambiente no Allure;</li>
 *   <li>{@code @Before}: traz o app para frente e volta para "Minhas tarefas", a partir de
 *       qualquer tela em que o cenário anterior tenha parado;</li>
 *   <li>{@code @After}: exclui a massa criada e restaura as preferências alteradas;</li>
 *   <li>{@code @AfterAll}: encerra a sessão Appium.</li>
 * </ul>
 * Logs, screenshots de cada passo e evidências de falha não ficam aqui: são anexados pelo plugin
 * {@link support.RegistroEtapas}, dentro do próprio passo no Allure. Um {@code @AfterStep} faria
 * cada screenshot aparecer como um passo extra ("hooks.Ganchos...") entre os passos do cenário.
 */
public class Ganchos {

    private static final Logger LOG = LoggerFactory.getLogger(Ganchos.class);
    private static final String EPICO = "Tasks.org - Android";
    private static final String GRUPO_CONCLUIDAS = "Concluída";
    /** Proteção contra laço infinito ao excluir tarefas repetidas deixadas por execuções anteriores. */
    private static final int LIMITE_EXCLUSOES_POR_TITULO = 3;

    private final ContextoCenario contexto;

    public Ganchos(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @BeforeAll
    public static void iniciarSessao() {
        // No dry-run o Cucumber só confere se todos os passos têm implementação: não há aparelho.
        if (Boolean.getBoolean("cucumber.execution.dry-run")) {
            return;
        }
        GerenciadorDriver.iniciarSessao();
        AllureAmbiente.registrar(GerenciadorDriver.obterDriver());
    }

    @AfterAll
    public static void encerrarSessao() {
        GerenciadorDriver.encerrarSessao();
    }

    @Before
    public void prepararTelaInicial() {
        Allure.epic(EPICO);
        LOG.info("Preparação: levando o app para \"Minhas tarefas\"");
        GerenciadorDriver.garantirAplicativoEmPrimeiroPlano();
        contexto.definirLista(new NavegacaoApp().irParaMinhasTarefas());
    }

    /**
     * Exclui as tarefas criadas pelo cenário e volta a exibir as concluídas, se o cenário as
     * ocultou. Roda mesmo quando o cenário falha, para não interferir nos cenários seguintes.
     * Uma falha na limpeza gera apenas um aviso: o resultado do cenário já foi definido.
     */
    @After(order = 10)
    public void limparMassa() {
        if (!GerenciadorDriver.sessaoAtiva()) {
            LOG.warn("Limpeza ignorada: a sessão Appium não está ativa");
            return;
        }
        LOG.info("Limpeza: tarefas a excluir {}", contexto.tarefasParaLimpar());
        try {
            TelaListaTarefas lista = new NavegacaoApp().irParaMinhasTarefas();
            if (contexto.exibicaoConcluidasAlterada()) {
                LOG.info("Limpeza: voltando a exibir as tarefas concluídas");
                lista = lista.abrirOrdenacao().definirMostrarConcluidas(true).fechar();
            }
            for (String tarefa : contexto.tarefasParaLimpar()) {
                lista = excluirSeExistir(lista, tarefa);
            }
            LOG.info("Limpeza concluída");
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível concluir a limpeza da massa do cenário: {}", e.getMessage());
            LOG.debug("Detalhes do erro na limpeza", e);
        }
    }

    /** Exclui a tarefa, procurando também no grupo "Concluída", que pode estar recolhido. */
    private static TelaListaTarefas excluirSeExistir(TelaListaTarefas lista, String tarefa) {
        if (!lista.exibeTarefa(tarefa) && lista.exibeGrupo(GRUPO_CONCLUIDAS)) {
            lista.expandirGrupo(GRUPO_CONCLUIDAS);
        }
        for (int i = 0; i < LIMITE_EXCLUSOES_POR_TITULO && lista.exibeTarefa(tarefa); i++) {
            LOG.info("Limpeza: excluindo a tarefa \"{}\"", tarefa);
            lista = lista.abrirTarefa(tarefa).excluir();
        }
        return lista;
    }
}
