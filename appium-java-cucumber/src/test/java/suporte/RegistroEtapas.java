package suporte;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin do Cucumber que escreve no terminal cada cenário e cada passo executado:
 * <pre>
 * INFO  [CENARIO] Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa
 * INFO  [ETAPA] Dado que existe a lista "Projetos de TI"
 * </pre>
 * Também guarda o texto do passo em andamento, usado pelos hooks para dar nome aos screenshots.
 * <p>
 * Registrado na chave {@code cucumber.plugin} do {@code junit-platform.properties}.
 */
public class RegistroEtapas implements ConcurrentEventListener {

    private static final Logger LOG_CENARIO = LoggerFactory.getLogger("CENARIO");
    private static final Logger LOG_ETAPA = LoggerFactory.getLogger("ETAPA");
    /** A execução é sequencial, mas o ThreadLocal evita mistura caso o paralelismo seja ligado. */
    private static final ThreadLocal<String> ETAPA_ATUAL = ThreadLocal.withInitial(() -> "");

    /** Texto do passo em execução, com a palavra-chave (ex.: {@code Quando eu busco por "Revisar"}). */
    public static String etapaAtual() {
        return ETAPA_ATUAL.get();
    }

    @Override
    public void setEventPublisher(EventPublisher publicador) {
        publicador.registerHandlerFor(TestCaseStarted.class,
                evento -> LOG_CENARIO.info(evento.getTestCase().getName()));
        publicador.registerHandlerFor(TestStepStarted.class, this::aoIniciarEtapa);
        publicador.registerHandlerFor(TestStepFinished.class, this::aoFinalizarEtapa);
    }

    private void aoIniciarEtapa(TestStepStarted evento) {
        if (evento.getTestStep() instanceof PickleStepTestStep passo) {
            String texto = passo.getStep().getKeyword() + passo.getStep().getText();
            ETAPA_ATUAL.set(texto);
            LOG_ETAPA.info(texto);
        }
    }

    private void aoFinalizarEtapa(TestStepFinished evento) {
        if (evento.getTestStep() instanceof PickleStepTestStep passo) {
            Status status = evento.getResult().getStatus();
            if (status != Status.PASSED) {
                LOG_ETAPA.warn("{} -> {}", passo.getStep().getText(), status);
            }
        }
    }
}
