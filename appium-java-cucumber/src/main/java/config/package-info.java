/**
 * Leitura das configurações da automação (servidor Appium, aplicativo, aparelho e tempos de espera).
 * <p>
 * Os valores ficam em {@code src/test/resources/config.properties} (padrão, versionado) e em
 * {@code config.local.properties} (dados do seu aparelho, fora do Git). Qualquer chave pode ser
 * sobrescrita na linha de comando com {@code -Dchave=valor}. Ver {@link config.Configuracao}.
 */
package config;
