package passos;

import driver.GerenciadorDriver;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import pages.MenuLateral;
import pages.TelaEdicaoTarefa;
import pages.TelaListaTarefas;
import suporte.ContextoCenario;
import ganchos.TiposParametro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos de preparação da massa e de cadastro de tarefas e listas.
 * <p>
 * Toda tarefa criada aqui é registrada no {@link ContextoCenario} e excluída pelo hook de
 * limpeza ao final do cenário.
 */
public class PassosCadastro {

    private static final String MINHAS_TAREFAS = "Minhas tarefas";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Campos aceitos na tabela do passo de cadastro, na ordem em que são preenchidos na tela. */
    private static final Set<String> CAMPOS_CADASTRO = Set.of("vencimento", "prioridade", "lista", "subtarefa");

    private final ContextoCenario contexto;

    public PassosCadastro(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /**
     * Garante que a lista local existe, criando-a só quando ainda não aparece no menu.
     * Ao final, volta para "Minhas tarefas".
     */
    @Dado("que existe a lista {string}")
    public void queExisteALista(String nomeLista) {
        MenuLateral menu = contexto.lista().abrirMenuLateral();
        if (!menu.exibeOpcao(nomeLista)) {
            menu.criarLista(nomeLista);
            menu = contexto.lista().abrirMenuLateral();
        }
        TelaListaTarefas lista = menu.selecionar(MINHAS_TAREFAS);
        assertThat(lista.obterTituloDaTela()).isEqualTo(MINHAS_TAREFAS);
        contexto.definirLista(lista);
    }

    @Dado("que existe a tarefa {string}")
    public void queExisteATarefa(String titulo) {
        contexto.definirLista(contexto.lista().criarTarefa(titulo));
        contexto.registrarTarefaCriada(titulo);
        assertThat(contexto.lista().exibeTarefa(titulo))
                .as("Tarefa \"%s\" exibida após o cadastro", titulo)
                .isTrue();
    }

    /** Tabela com as colunas "título" e "prioridade". */
    @Dado("que existem as tarefas:")
    public void queExistemAsTarefas(List<Map<String, String>> tarefas) {
        for (Map<String, String> tarefa : tarefas) {
            String titulo = tarefa.get("título");
            TelaEdicaoTarefa edicao = contexto.lista().tocarEmNovaTarefa().informarTitulo(titulo);
            if (tarefa.containsKey("prioridade")) {
                edicao.definirPrioridade(TiposParametro.paraPrioridade(tarefa.get("prioridade")));
            }
            contexto.definirLista(edicao.salvar());
            contexto.registrarTarefaCriada(titulo);
        }
        List<String> titulos = tarefas.stream().map(tarefa -> tarefa.get("título")).toList();
        assertThat(contexto.lista().obterTitulosDasTarefas()).containsAll(titulos);
    }

    /**
     * Cadastra a tarefa preenchendo os campos da tabela, todos opcionais:
     * <ul>
     *   <li>{@code vencimento}: "hoje", "ontem", "amanhã" ou uma data no formato dd/MM/aaaa;</li>
     *   <li>{@code prioridade}: nenhuma, baixa, média ou alta;</li>
     *   <li>{@code lista}: nome de uma lista existente;</li>
     *   <li>{@code subtarefa}: título da subtarefa.</li>
     * </ul>
     */
    @Quando("eu cadastro a tarefa {string} com os dados:")
    public void euCadastroATarefaComOsDados(String titulo, Map<String, String> dados) {
        assertThat(CAMPOS_CADASTRO)
                .as("Campos aceitos na tabela de cadastro")
                .containsAll(dados.keySet());

        TelaEdicaoTarefa edicao = contexto.lista().tocarEmNovaTarefa().informarTitulo(titulo);
        if (dados.containsKey("vencimento")) {
            definirVencimento(edicao, dados.get("vencimento"));
        }
        if (dados.containsKey("prioridade")) {
            edicao.definirPrioridade(TiposParametro.paraPrioridade(dados.get("prioridade")));
        }
        if (dados.containsKey("lista")) {
            edicao.selecionarLista(dados.get("lista"));
        }
        if (dados.containsKey("subtarefa")) {
            edicao.adicionarSubtarefa(dados.get("subtarefa"));
        }
        contexto.definirLista(edicao.salvar());
        contexto.registrarTarefaCriada(titulo);
    }

    /** "Hoje" usa o atalho do seletor; as demais datas são escolhidas no calendário. */
    private static void definirVencimento(TelaEdicaoTarefa edicao, String vencimento) {
        String valor = vencimento.trim().toLowerCase();
        if (valor.equals("hoje")) {
            edicao.definirVencimentoHoje();
            return;
        }
        LocalDate hoje = GerenciadorDriver.dataDoAparelho();
        LocalDate data = switch (valor) {
            case "ontem" -> hoje.minusDays(1);
            case "amanhã", "amanha" -> hoje.plusDays(1);
            default -> LocalDate.parse(valor, FORMATO_DATA);
        };
        edicao.definirVencimento(data);
    }
}
