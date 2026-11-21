package base;

import driver.GerenciadorDriver;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extensão JUnit que inicia uma única sessão Appium para toda a suíte
 * e a encerra ao final da execução.
 */
public class ExtensaoAppium implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ExtensaoAppium.class);

    @Override
    public void beforeAll(ExtensionContext contexto) {
        // O recurso fica no contexto raiz: é fechado automaticamente ao fim de toda a execução.
        contexto.getRoot().getStore(NAMESPACE)
                .computeIfAbsent("sessao-appium", chave -> new SessaoAppium(), SessaoAppium.class);
    }

    /** Recurso que mantém a sessão viva durante a suíte. */
    private static final class SessaoAppium implements AutoCloseable {

        private SessaoAppium() {
            GerenciadorDriver.iniciarSessao();
        }

        @Override
        public void close() {
            GerenciadorDriver.encerrarSessao();
        }
    }
}
