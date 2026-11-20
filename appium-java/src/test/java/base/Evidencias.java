package base;

import driver.GerenciadorDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/** Coleta de evidências anexadas ao relatório Allure. */
public final class Evidencias {

    private static final Logger LOG = LoggerFactory.getLogger(Evidencias.class);

    private Evidencias() {
    }

    public static void anexarScreenshot(String nome) {
        try {
            byte[] imagem = GerenciadorDriver.obterDriver().getScreenshotAs(OutputType.BYTES);
            Allure.attachment(nome, "image/png", new ByteArrayInputStream(imagem), AttachmentOptions.withFileExtension(".png"));
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível capturar o screenshot: {}", e.getMessage());
        }
    }

    public static void anexarPageSource(String nome) {
        try {
            String xml = GerenciadorDriver.obterDriver().getPageSource();
            Allure.attachment(nome, "text/xml",
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), AttachmentOptions.withFileExtension(".xml"));
        } catch (RuntimeException e) {
            LOG.warn("Não foi possível capturar a hierarquia da tela: {}", e.getMessage());
        }
    }
}
