package base;

import config.Configuracao;
import driver.GerenciadorDriver;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extensão JUnit que:
 * <ul>
 *   <li>inicia uma única sessão Appium para toda a suíte e a encerra ao final;</li>
 *   <li>registra o ambiente da execução na visão geral do relatório Allure;</li>
 *   <li>anexa evidências (screenshot e page source) ao relatório Allure.</li>
 * </ul>
 */
public class ExtensaoAppium implements BeforeAllCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ExtensaoAppium.class);

    @Override
    public void beforeAll(ExtensionContext contexto) {
        // O recurso fica no contexto raiz: é fechado automaticamente ao fim de toda a execução.
        contexto.getRoot().getStore(NAMESPACE)
                .computeIfAbsent("sessao-appium", chave -> new SessaoAppium(), SessaoAppium.class);
    }

    @Override
    public void afterTestExecution(ExtensionContext contexto) {
        if (!GerenciadorDriver.sessaoAtiva()) {
            return;
        }
        boolean falhou = contexto.getExecutionException().isPresent();
        if (falhou) {
            Evidencias.anexarScreenshot("Tela no momento da falha");
            Evidencias.anexarPageSource("Hierarquia da tela no momento da falha");
        } else if (Configuracao.obterBooleano("evidencia.sempre")) {
            Evidencias.anexarScreenshot("Tela ao final do teste");
        }
    }

    /** Recurso que mantém a sessão viva durante a suíte. */
    private static final class SessaoAppium implements AutoCloseable {

        private SessaoAppium() {
            GerenciadorDriver.iniciarSessao();
            AllureAmbiente.registrar(GerenciadorDriver.obterDriver());
        }

        @Override
        public void close() {
            GerenciadorDriver.encerrarSessao();
        }
    }
}
