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
 */
@Epic("Tasks.org - Android")
@ExtendWith(ExtensaoAppium.class)
public abstract class TesteBase {

    private static final Logger LOG = LoggerFactory.getLogger("ETAPA");

    protected TelaListaTarefas lista;

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
