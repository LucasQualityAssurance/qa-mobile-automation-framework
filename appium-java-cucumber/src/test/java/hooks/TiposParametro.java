package hooks;

import io.cucumber.java.ParameterType;
import pages.Prioridade;

/**
 * Tipos de parâmetro usados nas expressões dos passos.
 * <p>
 * {@code {prioridade}} aceita o nome da prioridade como aparece nos cenários ("nenhuma", "baixa",
 * "média" ou "alta") e o converte para o enum {@link Prioridade} usado pelos Page Objects.
 */
public class TiposParametro {

    @ParameterType("nenhuma|baixa|média|alta")
    public Prioridade prioridade(String nome) {
        return paraPrioridade(nome);
    }

    /** Converte o nome escrito no cenário (também usado nas tabelas) para o enum. */
    public static Prioridade paraPrioridade(String nome) {
        return switch (nome.trim().toLowerCase()) {
            case "nenhuma" -> Prioridade.NENHUMA;
            case "baixa" -> Prioridade.BAIXA;
            case "média", "media" -> Prioridade.MEDIA;
            case "alta" -> Prioridade.ALTA;
            default -> throw new IllegalArgumentException(
                    "Prioridade desconhecida: \"" + nome + "\". Use nenhuma, baixa, média ou alta.");
        };
    }
}
