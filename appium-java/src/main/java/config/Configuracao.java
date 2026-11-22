package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * Centraliza a leitura das configurações da automação.
 * Ordem de prioridade (da maior para a menor):
 * <ol>
 *   <li>propriedades de sistema ({@code -Dchave=valor});</li>
 *   <li>{@code config.local.properties}: dados do aparelho de cada pessoa, ignorado pelo Git;</li>
 *   <li>{@code config.properties}: valores padrão versionados no repositório.</li>
 * </ol>
 * Os arquivos ficam em {@code src/test/resources} e são lidos uma única vez, quando a classe
 * é usada pela primeira vez. Para criar uma configuração nova, adicione a chave no
 * {@code config.properties} e leia com {@link #obter(String)} ou um dos métodos tipados.
 */
public final class Configuracao {

    private static final String ARQUIVO_PADRAO = "config.properties";
    private static final String ARQUIVO_LOCAL = "config.local.properties";
    private static final Properties PROPRIEDADES = carregar();

    private Configuracao() {
    }

    /** Carrega o arquivo padrão (obrigatório) e, por cima dele, o local (opcional). */
    private static Properties carregar() {
        Properties propriedades = new Properties();
        if (!carregarArquivo(ARQUIVO_PADRAO, propriedades)) {
            throw new IllegalStateException("Arquivo de configuração não encontrado no classpath: " + ARQUIVO_PADRAO);
        }
        carregarArquivo(ARQUIVO_LOCAL, propriedades);
        return propriedades;
    }

    private static boolean carregarArquivo(String arquivo, Properties destino) {
        try (InputStream entrada = Thread.currentThread().getContextClassLoader().getResourceAsStream(arquivo)) {
            if (entrada == null) {
                return false;
            }
            destino.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
    }

    /** Retorna o valor da chave, ou vazio quando ela não foi configurada. */
    public static Optional<String> obterOpcional(String chave) {
        String valor = System.getProperty(chave, PROPRIEDADES.getProperty(chave));
        return valor == null || valor.isBlank() ? Optional.empty() : Optional.of(valor.trim());
    }

    /** Retorna o valor de uma chave obrigatória. */
    public static String obter(String chave) {
        return obterOpcional(chave)
                .orElseThrow(() -> new IllegalStateException("Configuração obrigatória ausente: " + chave));
    }

    /** Chave obrigatória com valor {@code true} ou {@code false}. */
    public static boolean obterBooleano(String chave) {
        return Boolean.parseBoolean(obter(chave));
    }

    /** Chave obrigatória com um número de segundos, devolvido como {@link Duration}. */
    public static Duration obterSegundos(String chave) {
        return Duration.ofSeconds(Long.parseLong(obter(chave)));
    }

    // ---------- Atalhos para as chaves mais usadas ----------

    /** Endereço do servidor Appium ({@code appium.url}), por padrão http://127.0.0.1:4723. */
    public static URL urlAppium() {
        try {
            return URI.create(obter("appium.url")).toURL();
        } catch (Exception e) {
            throw new IllegalStateException("URL do Appium inválida: " + obter("appium.url"), e);
        }
    }

    /** Pacote do aplicativo testado ({@code app.pacote}), ex.: org.tasks. */
    public static String pacoteApp() {
        return obter("app.pacote");
    }

    /** Tempo máximo para esperar um elemento antes de falhar ({@code tempo.espera.padrao}). */
    public static Duration esperaPadrao() {
        return obterSegundos("tempo.espera.padrao");
    }

    /** Tempo usado nas verificações que não devem falhar, como "está visível?" ({@code tempo.espera.curta}). */
    public static Duration esperaCurta() {
        return obterSegundos("tempo.espera.curta");
    }
}
