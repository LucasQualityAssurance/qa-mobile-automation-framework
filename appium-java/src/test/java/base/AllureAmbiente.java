package base;

import config.Configuracao;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Capabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Grava o arquivo {@code environment.properties} lido pelo Allure, para que a visão geral do
 * relatório mostre onde e como os testes rodaram (aparelho, versão do Android, aplicativo,
 * servidor Appium, versão do Java).
 * <p>
 * Os dados do aparelho são lidos da sessão Appium já iniciada, e não do arquivo de configuração,
 * então aparecem mesmo quando o aparelho foi encontrado automaticamente.
 */
final class AllureAmbiente {

    private static final Logger LOG = LoggerFactory.getLogger(AllureAmbiente.class);
    private static final String NAO_INFORMADO = "não informado";

    private AllureAmbiente() {
    }

    static void registrar(AndroidDriver driver) {
        Capabilities sessao = driver.getCapabilities();
        String fabricante = capability(sessao, "deviceManufacturer");
        String modelo = capability(sessao, "deviceModel");

        Properties ambiente = new Properties();
        ambiente.setProperty("Plataforma", Configuracao.obter("plataforma.nome"));
        ambiente.setProperty("Versão do Android", capability(sessao, "platformVersion"));
        ambiente.setProperty("Aparelho", (fabricante + " " + modelo).replace(NAO_INFORMADO + " ", "").trim());
        ambiente.setProperty("Aplicativo", Configuracao.pacoteApp());
        ambiente.setProperty("Automação", Configuracao.obter("automacao.nome"));
        ambiente.setProperty("Servidor Appium", Configuracao.urlAppium().toString());
        ambiente.setProperty("Java", System.getProperty("java.version"));
        ambiente.setProperty("Sistema", System.getProperty("os.name"));

        Path diretorioResultados = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        try {
            Files.createDirectories(diretorioResultados);
            try (OutputStream saida = Files.newOutputStream(diretorioResultados.resolve("environment.properties"))) {
                ambiente.store(saida, null);
            }
        } catch (IOException e) {
            // O ambiente é só informativo: a falha ao gravá-lo não deve interromper a suíte.
            LOG.warn("Não foi possível gravar o ambiente do Allure: {}", e.getMessage());
        }
    }

    /** Lê a capability com ou sem o prefixo "appium:", conforme o servidor a devolve. */
    private static String capability(Capabilities sessao, String nome) {
        Object valor = sessao.getCapability("appium:" + nome);
        if (valor == null) {
            valor = sessao.getCapability(nome);
        }
        return valor == null || valor.toString().isBlank() ? NAO_INFORMADO : valor.toString();
    }
}
