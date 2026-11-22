package base;

import driver.GerenciadorDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.NavegacaoApp;
import pages.TelaListaTarefas;

/**
 * Base de todos os testes. O aplicativo permanece aberto durante toda a suíte:
 * antes de cada teste ele apenas é levado de volta à lista "Minhas tarefas".
 * <p>
 * Ao estender esta classe, o teste recebe:
 * <ul>
 *   <li>a sessão Appium já aberta (pela {@link ExtensaoAppium});</li>
 *   <li>a variável {@link #lista}, já posicionada na tela "Minhas tarefas";</li>
 *   <li>o método {@link #etapa(String, Runnable)}, para registrar cada passo do fluxo;</li>
 *   <li>screenshot ao final do teste e, se ele falhar, screenshot e XML da tela no relatório.</li>
 * </ul>
 * Exemplo:
 * <pre>{@code
 * etapa("1. Criar a tarefa", () -> {
 *     lista.criarTarefa("Atualizar dependências");
 *     assertThat(lista.exibeTarefa("Atualizar dependências")).isTrue();
 * });
 * }</pre>
 */
@Epic("Tasks.org - Android")
@ExtendWith(ExtensaoAppium.class)
public abstract class TesteBase {

    /** Logger com o nome "ETAPA": é o que aparece como [ETAPA] nas linhas do terminal. */
    private static final Logger LOG = LoggerFactory.getLogger("ETAPA");

    /**
     * Tela principal, ponto de partida de todo teste. Reatribua quando um método devolver uma nova
     * instância (ex.: {@code lista = edicao.salvar();}).
     */
    protected TelaListaTarefas lista;

    /**
     * Roda antes de cada teste: traz o app para frente, se outro app estiver por cima, e volta
     * para "Minhas tarefas" a partir de qualquer tela em que o teste anterior tenha parado.
     */
    @BeforeEach
    void prepararTelaInicial() {
        GerenciadorDriver.garantirAplicativoEmPrimeiroPlano();
        lista = new NavegacaoApp().irParaMinhasTarefas();
    }

    /**
     * Executa uma etapa do fluxo: registra a descrição no log da execução e como
     * passo no relatório Allure, com o screenshot da tela ao final da etapa.
     */
    protected void etapa(String descricao, Runnable acao) {
        LOG.info(descricao);
        Allure.step(descricao, () -> {
            acao.run();
            Evidencias.anexarScreenshot(descricao);
        });
    }
}
