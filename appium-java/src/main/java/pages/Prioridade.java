package pages;

/**
 * Prioridades da tarefa, na ordem em que os botões aparecem na tela de edição
 * (da esquerda para a direita).
 */
public enum Prioridade {

    NENHUMA(0),
    BAIXA(1),
    MEDIA(2),
    ALTA(3);

    private final int posicao;

    Prioridade(int posicao) {
        this.posicao = posicao;
    }

    public int posicao() {
        return posicao;
    }

    public static Prioridade daPosicao(int posicao) {
        for (Prioridade prioridade : values()) {
            if (prioridade.posicao == posicao) {
                return prioridade;
            }
        }
        throw new IllegalArgumentException("Posição de prioridade inválida: " + posicao);
    }
}
