package support;

import config.Configuracao;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Plugin do Cucumber que registra a execução no terminal e no relatório Allure.
 * <p>
 * No terminal, cada cenário e cada passo executado:
 * <pre>
 * INFO  [CENARIO] Cadastrar tarefa completa com vencimento, prioridade, lista e subtarefa
 * INFO  [ETAPA] Dado que existe a lista "Projetos de TI"
 * INFO  [TelaListaTarefas] Tocar: ...
 * </pre>
 * No Allure, dentro de cada passo do cenário, nesta ordem:
 * <ul>
 *   <li>"Tela após a etapa": screenshot ao final do passo (quando {@code evidencia.sempre=true});</li>
 *   <li>em caso de falha (passo ou hook): screenshot e hierarquia da tela no momento do erro;</li>
 *   <li>"Log da etapa": as linhas de log registradas durante o passo, inclusive o DEBUG das
 *       esperas e localizadores, que não aparece no terminal.</li>
 * </ul>
 * No corpo do cenário, sem precisar expandir nenhum passo:
 * <ul>
 *   <li>"Tela ao final do cenário" (ou "Tela no momento da falha"): o último print tirado;</li>
 *   <li>"Log completo do cenário": tudo, inclusive hooks e limpeza da massa.</li>
 * </ul>
 * No início da execução, apaga os resultados e as evidências da execução anterior
 * ({@link LimpezaResultadosAllure}).
 * <p>
 * <b>Ordem dos plugins:</b> este plugin precisa vir <b>antes</b> do plugin do Allure na chave
 * {@code cucumber.plugin}. Assim ele recebe o fim do passo enquanto o Allure ainda o mantém
 * aberto, e os anexos ficam dentro do passo certo.
 */
public class RegistroEtapas implements ConcurrentEventListener {

    private static final Logger LOG_CENARIO = LoggerFactory.getLogger("CENARIO");
    private static final Logger LOG_ETAPA = LoggerFactory.getLogger("ETAPA");
    private static final AttachmentOptions COMO_LOG = AttachmentOptions.withFileExtension(".log");
    private static final AttachmentOptions COMO_XML = AttachmentOptions.withFileExtension(".xml");
    private static final AttachmentOptions COMO_PNG = AttachmentOptions.withFileExtension(".png");
    /** Último print do cenário em andamento, repetido no corpo do cenário ao final. */
    private static final ThreadLocal<byte[]> ULTIMO_PRINT = new ThreadLocal<>();

    @Override
    public void setEventPublisher(EventPublisher publicador) {
        // Antes de qualquer cenário: apaga resultados e evidências da execução anterior.
        publicador.registerHandlerFor(TestRunStarted.class, evento -> LimpezaResultadosAllure.limpar());
        publicador.registerHandlerFor(TestCaseStarted.class, this::aoIniciarCenario);
        publicador.registerHandlerFor(TestStepStarted.class, this::aoIniciarEtapa);
        publicador.registerHandlerFor(TestStepFinished.class, this::aoFinalizarEtapa);
        publicador.registerHandlerFor(TestCaseFinished.class, this::aoFinalizarCenario);
    }

    // ---------- Cenário ----------

    private void aoIniciarCenario(TestCaseStarted evento) {
        CapturaLog.iniciarCenario();
        TestCase cenario = evento.getTestCase();
        LOG_CENARIO.info("{}", cenario.getName());
        LOG_CENARIO.debug("Arquivo: {} (linha {}) | Tags: {}",
                cenario.getUri(), cenario.getLocation().getLine(), cenario.getTags());
    }

    private void aoFinalizarCenario(TestCaseFinished evento) {
        Result resultado = evento.getResult();
        LOG_CENARIO.info("Resultado: {} em {}", traduzir(resultado.getStatus()), formatarDuracao(resultado));
        // Neste ponto o passo atual do Allure já foi fechado: os anexos vão direto para o cenário (raiz).
        byte[] ultimoPrint = ULTIMO_PRINT.get();
        ULTIMO_PRINT.remove();
        if (ultimoPrint != null) {
            anexarNoCenario(resultado.getStatus() == Status.FAILED
                    ? "Tela no momento da falha" : "Tela ao final do cenário",
                    "image/png", new ByteArrayInputStream(ultimoPrint), COMO_PNG);
        }
        String log = CapturaLog.coletarCenario();
        if (!log.isBlank()) {
            anexarNoCenario("Log completo do cenário", "text/plain", texto(log), COMO_LOG);
        }
    }

    private static void anexarNoCenario(String nome, String tipo, InputStream conteudo, AttachmentOptions opcoes) {
        Allure.getLifecycle().getCurrentRootKey().ifPresent(cenario ->
                Allure.getLifecycle().addAttachment(cenario, nome, tipo, conteudo, opcoes));
    }

    // ---------- Etapas (passos e hooks) ----------

    private void aoIniciarEtapa(TestStepStarted evento) {
        CapturaLog.iniciarEtapa();
        if (evento.getTestStep() instanceof PickleStepTestStep) {
            LOG_ETAPA.info(descrever(evento.getTestStep()));
        }
    }

    private void aoFinalizarEtapa(TestStepFinished evento) {
        TestStep etapa = evento.getTestStep();
        Result resultado = evento.getResult();
        Status status = resultado.getStatus();
        String descricao = descrever(etapa);

        if (status == Status.FAILED) {
            LOG_ETAPA.error("FALHOU: {} ({}): {}", descricao, formatarDuracao(resultado),
                    resultado.getError() == null ? "sem detalhes" : resultado.getError().getMessage());
        } else if (status == Status.PASSED) {
            LOG_ETAPA.debug("Concluída: {} ({})", descricao, formatarDuracao(resultado));
        } else {
            LOG_ETAPA.warn("{}: {}", traduzir(status), descricao);
        }

        boolean passoDoCenario = etapa instanceof PickleStepTestStep;
        // O print vem primeiro: é o primeiro anexo que aparece ao abrir o passo no Allure.
        if (status == Status.FAILED) {
            anexarEvidenciasDaFalha();
        } else if (passoDoCenario && status == Status.PASSED && Configuracao.obterBooleano("evidencia.sempre")) {
            // Só passos que rodaram: um passo pulado após uma falha não tem tela própria para mostrar.
            Evidencias.capturarTela().ifPresent(imagem -> anexarPrint("Tela após a etapa", imagem));
        }
        // Os hooks que passam só aparecem no log do cenário: anexá-los poluiria o relatório.
        if (passoDoCenario || status == Status.FAILED) {
            anexarTexto("Log da etapa", CapturaLog.coletarEtapa());
        }
    }

    private static void anexarEvidenciasDaFalha() {
        Evidencias.capturarTela().ifPresent(imagem -> anexarPrint("Tela no momento da falha", imagem));
        Evidencias.capturarHierarquia().ifPresent(xml -> anexarNaEtapa(
                "Hierarquia da tela no momento da falha", "text/xml", texto(xml), COMO_XML));
    }

    /**
     * Anexa o print no passo e o guarda como o último do cenário: ao final, ele é repetido no
     * corpo do cenário como "Tela ao final do cenário", visível sem expandir nenhum passo.
     * Usa o print já tirado (e não um novo) porque, ao final, a limpeza da massa já mudou a tela.
     */
    private static void anexarPrint(String nome, byte[] imagem) {
        anexarImagem(nome, imagem);
        ULTIMO_PRINT.set(imagem);
    }

    // ---------- Utilitários ----------

    private static String descrever(TestStep etapa) {
        if (etapa instanceof PickleStepTestStep passo) {
            return passo.getStep().getKeyword() + passo.getStep().getText();
        }
        if (etapa instanceof HookTestStep gancho) {
            return "hook " + gancho.getHookType() + " (" + gancho.getCodeLocation() + ")";
        }
        return etapa.getCodeLocation();
    }

    private static void anexarTexto(String nome, String conteudo) {
        if (!conteudo.isBlank()) {
            anexarNaEtapa(nome, "text/plain", texto(conteudo), COMO_LOG);
        }
    }

    private static void anexarImagem(String nome, byte[] imagem) {
        anexarNaEtapa(nome, "image/png", new ByteArrayInputStream(imagem), COMO_PNG);
    }

    /**
     * Anexa diretamente no passo (ou hook) que está aberto no Allure. O {@code Allure.attachment}
     * criaria um subpasso extra só para segurar o anexo, poluindo a árvore de passos do relatório.
     */
    private static void anexarNaEtapa(String nome, String tipo, InputStream conteudo, AttachmentOptions opcoes) {
        Allure.getLifecycle().addAttachment(nome, tipo, conteudo, opcoes);
    }

    private static InputStream texto(String conteudo) {
        return new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private static String formatarDuracao(Result resultado) {
        long ms = resultado.getDuration().toMillis();
        return ms < 1000 ? ms + " ms" : String.format("%.1f s", ms / 1000.0);
    }

    private static String traduzir(Status status) {
        return switch (status) {
            case PASSED -> "PASSOU";
            case FAILED -> "FALHOU";
            case SKIPPED -> "IGNORADA";
            case PENDING -> "PENDENTE";
            case UNDEFINED -> "SEM IMPLEMENTAÇÃO";
            case AMBIGUOUS -> "AMBÍGUA";
            case UNUSED -> "NÃO UTILIZADA";
        };
    }
}
