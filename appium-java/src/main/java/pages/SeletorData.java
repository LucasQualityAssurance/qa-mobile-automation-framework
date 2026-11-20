package pages;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Painel de seleção de data de vencimento. */
public class SeletorData extends BasePage {

    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", PORTUGUES_BRASIL);
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PORTUGUES_BRASIL);

    private static final By ATALHO_HOJE = AppiumBy.id("date_shortcut_today");
    private static final By BOTAO_OK = AppiumBy.id("date_picker_ok");
    private static final By MES_ANTERIOR = porDescricao("Mudar para o mês anterior");
    private static final By PROXIMO_MES = porDescricao("Mudar para o próximo mês");
    private static final int LIMITE_NAVEGACAO_MESES = 24;

    public SeletorData aguardarAbertura() {
        esperarVisivel(BOTAO_OK);
        return this;
    }

    @Step("Selecionar o atalho \"Hoje\"")
    public SeletorData selecionarHoje() {
        clicar(ATALHO_HOJE);
        return this;
    }

    @Step("Selecionar a data {data} no calendário")
    public SeletorData selecionarData(LocalDate data) {
        navegarAteMes(YearMonth.from(data));
        // Os dias do calendário têm o texto "Quinta-feira, 19 de novembro de 2026"
        clicar(porTextoContendo(", " + data.format(FORMATO_DIA)));
        return this;
    }

    @Step("Confirmar a data")
    public void confirmar() {
        clicar(BOTAO_OK);
        esperarDesaparecer(BOTAO_OK);
    }

    private void navegarAteMes(YearMonth destino) {
        YearMonth atual = YearMonth.now();
        int diferenca = (destino.getYear() - atual.getYear()) * 12 + destino.getMonthValue() - atual.getMonthValue();
        if (Math.abs(diferenca) > LIMITE_NAVEGACAO_MESES) {
            throw new IllegalArgumentException("Data muito distante para navegar no calendário: " + destino);
        }
        By botao = diferenca < 0 ? MES_ANTERIOR : PROXIMO_MES;
        for (int i = 0; i < Math.abs(diferenca); i++) {
            clicar(botao);
        }
        esperarVisivel(porTexto(destino.format(FORMATO_MES)));
    }
}
