package pages;

import driver.GerenciadorDriver;
import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Painel de seleção da data de vencimento.
 * <p>
 * <b>Quando aparece:</b> ao tocar na linha "Data de vencimento" da {@link TelaEdicaoTarefa}.
 * <p>
 * <b>O que tem na tela:</b> atalhos de data, como "Hoje", um calendário
 * mensal com setas para trocar de mês e o botão "OK", que confirma a escolha.
 * <p>
 * <b>Para onde vai:</b> ao confirmar, o painel fecha e a tela de edição mostra a data escolhida.
 * <p>
 * Os textos do calendário dependem do idioma do aparelho. Esta classe assume o aparelho em
 * português do Brasil.
 */
public class SeletorData extends BasePage {

    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");
    /** Formato do dia no calendário, ex.: "19 de novembro de 2026". */
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", PORTUGUES_BRASIL);
    /** Formato do mês exibido no topo do calendário, ex.: "novembro de 2026". */
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PORTUGUES_BRASIL);

    private static final By ATALHO_HOJE = AppiumBy.id("date_shortcut_today");
    private static final By BOTAO_OK = AppiumBy.id("date_picker_ok");
    private static final By MES_ANTERIOR = porDescricao("Mudar para o mês anterior");
    private static final By PROXIMO_MES = porDescricao("Mudar para o próximo mês");
    /** Proteção contra datas muito distantes, que exigiriam toques demais no calendário. */
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

    /** Navega até o mês da data e toca no dia. Aceita datas passadas e futuras. */
    @Step("Selecionar a data {data} no calendário")
    public SeletorData selecionarData(LocalDate data) {
        navegarAteMes(YearMonth.from(data));
        // Os dias do calendário têm o texto "Quinta-feira, 19 de novembro de 2026"
        clicar(porTextoContendo(", " + data.format(FORMATO_DIA)));
        return this;
    }

    /** Toca em "OK" e espera o painel fechar. */
    @Step("Confirmar a data")
    public void confirmar() {
        clicar(BOTAO_OK);
        esperarDesaparecer(BOTAO_OK);
    }

    /**
     * O calendário sempre abre no mês atual do aparelho: calcula quantos meses avançar ou voltar,
     * toca nas setas e confirma pelo título do mês. O mês atual vem do relógio do aparelho, e não
     * do computador, para não errar quando os fusos ou as datas forem diferentes.
     */
    private void navegarAteMes(YearMonth destino) {
        YearMonth atual = YearMonth.from(GerenciadorDriver.dataDoAparelho());
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
