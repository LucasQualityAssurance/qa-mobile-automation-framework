package driver;

import config.Configuracao;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Responsável pelo ciclo de vida da sessão Appium.
 * Uma única sessão é reaproveitada durante a suíte e o aplicativo permanece
 * aberto entre os testes.
 */
public final class GerenciadorDriver {

    private static final Logger LOG = LoggerFactory.getLogger(GerenciadorDriver.class);
    private static final ThreadLocal<AndroidDriver> DRIVER = new ThreadLocal<>();

    private GerenciadorDriver() {
    }

    public static AndroidDriver obterDriver() {
        AndroidDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("A sessão Appium ainda não foi iniciada.");
        }
        return driver;
    }

    public static boolean sessaoAtiva() {
        return DRIVER.get() != null;
    }

    public static void iniciarSessao() {
        if (sessaoAtiva()) {
            return;
        }
        LOG.info("Iniciando sessão Appium em {}", Configuracao.urlAppium());
        AndroidDriver driver = new AndroidDriver(Configuracao.urlAppium(), criarCapabilities());
        // Os testTags do Jetpack Compose não possuem o prefixo "org.tasks:id/".
        // Sem esta configuração, By.id("save_task") seria convertido para "org.tasks:id/save_task".
        driver.setSetting("disableIdLocatorAutocompletion", true);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        DRIVER.set(driver);
        prepararAplicativoParaSuite(driver);
    }

    public static void encerrarSessao() {
        AndroidDriver driver = DRIVER.get();
        if (driver != null) {
            LOG.info("Encerrando sessão Appium");
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }

    /**
     * Executado uma única vez, no início da suíte: se configurado, limpa os dados
     * do app para que a execução parta de um estado conhecido (tela de boas-vindas).
     */
    private static void prepararAplicativoParaSuite(AndroidDriver driver) {
        if (!Configuracao.obterBooleano("app.limpar.dados.inicio.suite")) {
            return;
        }
        String pacote = Configuracao.pacoteApp();
        LOG.info("Limpando os dados do aplicativo antes da suíte");
        driver.terminateApp(pacote);
        driver.executeScript("mobile: clearApp", Map.of("appId", pacote));
        // A limpeza revoga as permissões; a de notificação evita o aviso de lembretes desabilitados.
        driver.executeScript("mobile: changePermissions", Map.of(
                "permissions", List.of("android.permission.POST_NOTIFICATIONS"),
                "appPackage", pacote,
                "action", "grant",
                "target", "pm"));
        driver.activateApp(pacote);
    }

    /**
     * Entre os testes o app não é reiniciado: apenas é trazido para o primeiro
     * plano caso não esteja visível.
     */
    public static void garantirAplicativoEmPrimeiroPlano() {
        AndroidDriver driver = obterDriver();
        String pacote = Configuracao.pacoteApp();
        if (driver.queryAppState(pacote) != ApplicationState.RUNNING_IN_FOREGROUND) {
            LOG.info("Aplicativo fora do primeiro plano; reativando sem reiniciar");
            driver.activateApp(pacote);
        }
    }

    private static UiAutomator2Options criarCapabilities() {
        UiAutomator2Options opcoes = new UiAutomator2Options()
                .setPlatformName(Configuracao.obter("plataforma.nome"))
                .setAutomationName(Configuracao.obter("automacao.nome"))
                .setAppPackage(Configuracao.pacoteApp())
                .setAppActivity(Configuracao.obter("app.atividade"))
                .setAppWaitActivity(Configuracao.obter("app.atividade.espera"))
                .setNoReset(Configuracao.obterBooleano("app.sem.reset"))
                .setAutoGrantPermissions(Configuracao.obterBooleano("app.conceder.permissoes"))
                .setNewCommandTimeout(Configuracao.obterSegundos("tempo.comando.novo"));

        // Dados do aparelho são opcionais: com um único dispositivo conectado o Appium o encontra sozinho.
        Configuracao.obterOpcional("dispositivo.udid").ifPresent(opcoes::setUdid);
        Configuracao.obterOpcional("dispositivo.nome").ifPresent(opcoes::setDeviceName);
        Configuracao.obterOpcional("dispositivo.versao").ifPresent(opcoes::setPlatformVersion);
        return opcoes;
    }
}
