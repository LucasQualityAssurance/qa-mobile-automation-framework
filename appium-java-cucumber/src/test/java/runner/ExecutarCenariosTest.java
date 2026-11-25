package runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Ponto de entrada da execução: o Surefire (e o IntelliJ) encontram esta classe e a JUnit Platform
 * repassa ao Cucumber os cenários da pasta {@code src/test/resources/features}.
 * <p>
 * As demais configurações (glue, plugins, paralelismo) ficam no {@code junit-platform.properties}.
 * Para filtrar cenários, use tags: {@code mvn test -Dcucumber.filter.tags="@ciclo-vida"}.
 */
@Suite
@SuiteDisplayName("Tasks.org - Android")
@IncludeEngines("cucumber")
@SelectPackages("features")
public class ExecutarCenariosTest {
}
