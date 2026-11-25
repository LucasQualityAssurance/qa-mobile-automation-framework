package support;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.nio.charset.StandardCharsets;

/**
 * Appender do Logback que guarda em memória as linhas de log da execução, para que sejam
 * anexadas ao relatório Allure pelo plugin {@link RegistroEtapas}.
 * <p>
 * Mantém dois registros, ambos por thread:
 * <ul>
 *   <li><b>etapa</b>: só o que foi registrado durante o passo em andamento, anexado ao próprio
 *       passo no Allure ("Log da etapa");</li>
 *   <li><b>cenário</b>: tudo o que foi registrado do início ao fim do cenário, inclusive hooks e
 *       limpeza, anexado ao cenário ("Log completo do cenário").</li>
 * </ul>
 * Configurado no {@code logback-test.xml}, com nível DEBUG: o relatório recebe também os
 * detalhes (esperas e localizadores) que não aparecem no terminal.
 */
public class CapturaLog extends AppenderBase<ILoggingEvent> {

    private static final ThreadLocal<StringBuilder> LOG_ETAPA = ThreadLocal.withInitial(StringBuilder::new);
    private static final ThreadLocal<StringBuilder> LOG_CENARIO = ThreadLocal.withInitial(StringBuilder::new);

    private Encoder<ILoggingEvent> encoder;

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    public void start() {
        if (encoder == null) {
            addError("Nenhum encoder configurado para o appender " + name);
            return;
        }
        encoder.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent evento) {
        String linha = new String(encoder.encode(evento), StandardCharsets.UTF_8);
        LOG_ETAPA.get().append(linha);
        LOG_CENARIO.get().append(linha);
    }

    /** Começa um novo cenário: descarta o que sobrou do anterior. */
    public static void iniciarCenario() {
        LOG_CENARIO.get().setLength(0);
        LOG_ETAPA.get().setLength(0);
    }

    /** Começa uma nova etapa: o log da etapa passa a conter só o que vier a partir daqui. */
    public static void iniciarEtapa() {
        LOG_ETAPA.get().setLength(0);
    }

    /** Devolve o log da etapa em andamento e o esvazia. */
    public static String coletarEtapa() {
        return coletar(LOG_ETAPA.get());
    }

    /** Devolve o log completo do cenário e o esvazia. */
    public static String coletarCenario() {
        return coletar(LOG_CENARIO.get());
    }

    private static String coletar(StringBuilder registro) {
        String texto = registro.toString();
        registro.setLength(0);
        return texto;
    }
}
