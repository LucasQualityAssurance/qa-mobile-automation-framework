package support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Apaga os resultados e as evidências do Allure da execução anterior, para o relatório mostrar
 * somente a execução atual e a pasta {@code target} não crescer.
 * <p>
 * Chamada pelo {@link RegistroEtapas} no início de toda execução, antes de qualquer resultado ser
 * gravado. Vale para o Maven e também para a execução pela IDE, que não passa pela fase de
 * limpeza do Maven.
 * <p>
 * Para acumular várias execuções no mesmo relatório, use {@code -Dallure.manter.resultados=true}.
 */
final class LimpezaResultadosAllure {

    private static final Logger LOG = LoggerFactory.getLogger(LimpezaResultadosAllure.class);
    private static final String PASTA_ESPERADA = "allure-results";

    private LimpezaResultadosAllure() {
    }

    static void limpar() {
        if (Boolean.getBoolean("allure.manter.resultados")) {
            LOG.info("Resultados anteriores do Allure mantidos (allure.manter.resultados=true)");
            return;
        }
        Path pasta = Path.of(System.getProperty("allure.results.directory", "target/allure-results"))
                .toAbsolutePath().normalize();
        // Proteção: só apaga dentro de uma pasta chamada "allure-results", nunca outro diretório
        // que tenha sido configurado por engano.
        if (!pasta.getFileName().toString().equals(PASTA_ESPERADA) || !Files.isDirectory(pasta)) {
            return;
        }
        // Apaga o conteúdo e mantém a própria pasta: no Windows ela pode estar aberta por outro
        // programa (terminal, explorador), o que impediria removê-la.
        try (Stream<Path> itens = Files.walk(pasta)) {
            long apagados = itens.filter(item -> !item.equals(pasta))
                    .sorted(Comparator.reverseOrder())
                    .filter(LimpezaResultadosAllure::apagar)
                    .count();
            LOG.info("Resultados anteriores do Allure apagados: {} arquivo(s) em {}", apagados, pasta);
        } catch (IOException e) {
            LOG.warn("Não foi possível limpar os resultados anteriores do Allure: {}", e.getMessage());
        }
    }

    private static boolean apagar(Path item) {
        try {
            Files.delete(item);
            return true;
        } catch (IOException e) {
            LOG.warn("Não foi possível apagar {}: {}", item, e.getMessage());
            return false;
        }
    }
}
