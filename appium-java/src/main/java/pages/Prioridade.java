package pages;

/**
 * Prioridades da tarefa, na ordem em que os botões aparecem na tela de edição
 * (da esquerda para a direita).
 * <p>
 * Os botões de prioridade não têm id nem texto próprios; a única forma estável de distingui-los
 * é a posição. Por isso cada valor guarda o índice do seu botão. Na lista de tarefas, a prioridade
 * aparece como a cor do círculo de conclusão.
 */
public enum Prioridade {

    NENHUMA(0),
    BAIXA(1),
    MEDIA(2),
    ALTA(3);

    /** Índice do botão na tela de edição, começando em 0. */
    private final int posicao;

    Prioridade(int posicao) {
        this.posicao = posicao;
    }

    public int posicao() {
        return posicao;
    }

    /** Converte o índice do botão marcado na prioridade correspondente. */
    public static Prioridade daPosicao(int posicao) {
        for (Prioridade prioridade : values()) {
            if (prioridade.posicao == posicao) {
                return prioridade;
            }
        }
        throw new IllegalArgumentException("Posição de prioridade inválida: " + posicao);
    }
}
