package suporte;

import driver.GerenciadorDriver;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Captura das evidências anexadas ao relatório Allure pelos hooks da classe {@code ganchos.Ganchos}.
 * <p>
 * Uma falha ao capturar a evidência apenas gera um aviso no log e devolve vazio: ela nunca
 * derruba o cenário nem esconde o erro original.
 */
public final class Evidencias {

    private static final Logger LOG = LoggerFactory.getLogger(Evidencias.class);

    private Evidencias() {
    }

    /** Imagem PNG da tela atual do aparelho. */
    public static Optional<byte[]> capturarTela() {
        if (!GerenciadorDriver.sessaoAtiva()) {
            return Optional.empty();
        }
        try {
            return Optional.of(GerenciadorDriver.obterDriver().getScreenshotAs(OutputType.BYTES));
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível capturar o screenshot: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Hierarquia da tela (XML com todos os elementos, ids e textos). É a principal pista para
     * descobrir por que um localizador não encontrou o elemento.
     */
    public static Optional<String> capturarHierarquia() {
        if (!GerenciadorDriver.sessaoAtiva()) {
            return Optional.empty();
        }
        try {
            return Optional.of(GerenciadorDriver.obterDriver().getPageSource());
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível capturar a hierarquia da tela: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
