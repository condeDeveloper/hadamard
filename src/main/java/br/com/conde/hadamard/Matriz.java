package br.com.conde.hadamard;

/**
 * Matriz quadrada de +1 e -1, guardada como mascara de bits por linha: o bit
 * ligado quer dizer -1.
 *
 * <p>Uma matriz e de Hadamard quando quaisquer duas linhas diferentes sao
 * ortogonais — o produto interno delas da zero. Para linhas de +1 e -1, isso
 * quer dizer que elas concordam em exatamente metade das posicoes, e discordam
 * na outra metade. Guardar as linhas como bits transforma essa conta num
 * {@code xor} e um {@code bitCount}.
 */
public final class Matriz {

    private final int ordem;
    private final int[] linhas;

    public Matriz(int ordem, int[] linhas) {
        if (ordem < 1) {
            throw new IllegalArgumentException("ordem tem que ser positiva: " + ordem);
        }
        if (linhas.length != ordem) {
            throw new IllegalArgumentException(
                    "sao " + ordem + " linhas esperadas e vieram " + linhas.length);
        }
        this.ordem = ordem;
        this.linhas = linhas.clone();
    }

    public int ordem() {
        return ordem;
    }

    public int linha(int i) {
        return linhas[i];
    }

    /** O valor em (i,j): +1 ou -1. */
    public int valor(int i, int j) {
        return (linhas[i] & (1 << j)) == 0 ? 1 : -1;
    }

    /**
     * Duas linhas sao ortogonais quando discordam em exatamente metade das
     * colunas. O xor acende os bits onde elas diferem, e bitCount conta.
     */
    public static boolean ortogonais(int a, int b, int ordem) {
        return Integer.bitCount(a ^ b) * 2 == ordem;
    }

    /** True se toda dupla de linhas distintas e ortogonal. */
    public boolean ehHadamard() {
        for (int i = 0; i < ordem; i++) {
            for (int j = i + 1; j < ordem; j++) {
                if (!ortogonais(linhas[i], linhas[j], ordem)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** O produto interno de duas linhas, contado na marra, sem truque de bits. */
    public int produtoInterno(int i, int j) {
        int soma = 0;
        for (int coluna = 0; coluna < ordem; coluna++) {
            soma += valor(i, coluna) * valor(j, coluna);
        }
        return soma;
    }

    /**
     * True se as COLUNAS tambem sao ortogonais. Nao e exigido pela definicao,
     * mas e consequencia dela: se vale para linhas, vale para colunas.
     */
    public boolean colunasOrtogonais() {
        int[] colunas = new int[ordem];
        for (int j = 0; j < ordem; j++) {
            int mascara = 0;
            for (int i = 0; i < ordem; i++) {
                if (valor(i, j) == -1) {
                    mascara |= 1 << i;
                }
            }
            colunas[j] = mascara;
        }
        for (int a = 0; a < ordem; a++) {
            for (int b = a + 1; b < ordem; b++) {
                if (!ortogonais(colunas[a], colunas[b], ordem)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * A matriz esta na forma normalizada quando a primeira linha e a primeira
     * coluna sao todas +1. Toda matriz de Hadamard vira uma dessas trocando
     * sinais de linhas e colunas inteiras, o que nao desfaz a ortogonalidade.
     */
    public boolean normalizada() {
        if (linhas[0] != 0) {
            return false;
        }
        for (int i = 0; i < ordem; i++) {
            if (valor(i, 0) != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * A construcao de Sylvester: a partir de H, monta a matriz de ordem dobrada
     *
     * <pre>[ H   H ]
     * [ H  -H ]</pre>
     */
    public Matriz sylvester() {
        int nova = ordem * 2;
        if (nova > 30) {
            throw new IllegalArgumentException("ordem grande demais para mascara de int: " + nova);
        }
        int[] saida = new int[nova];
        for (int i = 0; i < ordem; i++) {
            int esquerda = linhas[i];
            int direita = linhas[i] << ordem;
            int direitaTrocada = (~linhas[i] & ((1 << ordem) - 1)) << ordem;
            saida[i] = esquerda | direita;
            saida[i + ordem] = esquerda | direitaTrocada;
        }
        return new Matriz(nova, saida);
    }

    /** A matriz de ordem 1, que e so o numero 1. */
    public static Matriz ordemUm() {
        return new Matriz(1, new int[] {0});
    }

    /** A matriz de Sylvester de ordem 2^k. */
    public static Matriz deSylvester(int potencia) {
        Matriz m = ordemUm();
        for (int i = 0; i < potencia; i++) {
            m = m.sylvester();
        }
        return m;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ordem; i++) {
            for (int j = 0; j < ordem; j++) {
                sb.append(valor(i, j) == 1 ? '+' : '-');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
