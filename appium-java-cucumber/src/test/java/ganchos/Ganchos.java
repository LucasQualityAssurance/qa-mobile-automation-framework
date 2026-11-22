package ganchos;

import config.Configuracao;
import driver.GerenciadorDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.NavegacaoApp;
import pages.TelaListaTarefas;
import suporte.AllureAmbiente;
import suporte.ContextoCenario;
import suporte.Evidencias;
import suporte.RegistroEtapas;

/**
 * Hooks do Cucumber: o que acontece antes e depois da execução, de cada cenário e de cada passo.
 * <ul>
 *   <li>{@code @BeforeAll}: abre uma única sessão Appium para toda a execução e registra o
 *       ambiente no Allure;</li>
 *   <li>{@code @Before}: traz o app para frente e volta para "Minhas tarefas", a partir de
 *       qualquer tela em que o cenário anterior tenha parado;</li>
 *   <li>{@code @AfterStep}: anexa o screenshot da tela ao final de cada passo;</li>
 *   <li>{@code @After}: anexa screenshot e hierarquia da tela quando o cenário falha e depois
 *       exclui a massa criada, restaurando as preferências alteradas;</li>
 *   <li>{@code @AfterAll}: encerra a sessão Appium.</li>
 * </ul>
 * Os hooks {@code @After} rodam do maior {@code order} para o menor: as evidências são
 * capturadas antes da limpeza, que muda a tela.
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
        GerenciadorDriver.garantirAplicativoEmPrimeiroPlano();
        contexto.definirLista(new NavegacaoApp().irParaMinhasTarefas());
    }

    @AfterStep
    public void anexarTelaDaEtapa(Scenario cenario) {
        if (Configuracao.obterBooleano("evidencia.sempre")) {
            Evidencias.capturarTela().ifPresent(imagem ->
                    cenario.attach(imagem, "image/png", "Tela após: " + RegistroEtapas.etapaAtual()));
        }
    }

    @After(order = 20)
    public void anexarEvidenciasDaFalha(Scenario cenario) {
        if (cenario.isFailed()) {
            Evidencias.capturarTela().ifPresent(imagem ->
                    cenario.attach(imagem, "image/png", "Tela no momento da falha"));
            Evidencias.capturarHierarquia().ifPresent(xml ->
                    cenario.attach(xml, "text/xml", "Hierarquia da tela no momento da falha"));
        }
    }

    /**
     * Exclui as tarefas criadas pelo cenário e volta a exibir as concluídas, se o cenário as
     * ocultou. Roda mesmo quando o cenário falha, para não interferir nos cenários seguintes.
     * Uma falha na limpeza gera apenas um aviso: o resultado do cenário já foi definido.
     */
    @After(order = 10)
    public void limparMassa() {
        if (!GerenciadorDriver.sessaoAtiva()) {
            return;
        }
        try {
            TelaListaTarefas lista = new NavegacaoApp().irParaMinhasTarefas();
            if (contexto.exibicaoConcluidasAlterada()) {
                lista = lista.abrirOrdenacao().definirMostrarConcluidas(true).fechar();
            }
            for (String tarefa : contexto.tarefasParaLimpar()) {
                lista = excluirSeExistir(lista, tarefa);
            }
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível concluir a limpeza da massa do cenário: {}", e.getMessage());
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
