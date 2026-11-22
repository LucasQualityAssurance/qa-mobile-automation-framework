/**
 * Criação e encerramento da sessão Appium (o "driver" que controla o aparelho).
 * <p>
 * Toda a suíte usa uma única sessão, aberta antes do primeiro teste e fechada depois do último.
 * Os Page Objects obtêm o driver por {@link driver.GerenciadorDriver#obterDriver()}.
 */
package driver;
