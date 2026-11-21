package base;

import driver.GerenciadorDriver;
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
@ExtendWith(ExtensaoAppium.class)
public abstract class TesteBase {

    private static final Logger LOG = LoggerFactory.getLogger("ETAPA");

    protected TelaListaTarefas lista;

    @BeforeEach
    void prepararTelaInicial() {
        GerenciadorDriver.garantirAplicativoEmPrimeiroPlano();
        lista = new NavegacaoApp().irParaMinhasTarefas();
    }

    /** Executa uma etapa do fluxo, registrando sua descrição no log da execução. */
    protected void etapa(String descricao, Runnable acao) {
        LOG.info(descricao);
        acao.run();
    }
}
