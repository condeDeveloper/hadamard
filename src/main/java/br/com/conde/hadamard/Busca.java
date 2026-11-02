package br.com.conde.hadamard;

import java.util.ArrayList;
import java.util.List;

/**
 * A busca completa: monta a matriz linha a linha, e so aceita uma linha nova se
 * ela for ortogonal a TODAS as anteriores.
 *
 * <p>Sem a poda, procurar uma matriz de ordem 8 seria olhar 2^64 tabuleiros.
 * Com ela, a busca nunca desce um ramo que ja esta errado, e a ordem 8 sai
 * instantaneamente. A poda nao pula resposta nenhuma: uma linha que nao e
 * ortogonal as anteriores nao vira ortogonal depois.
 *
 * <p>A busca trabalha com matrizes NORMALIZADAS — primeira linha e primeira
 * coluna todas +1. Toda matriz de Hadamard pode ser levada a essa forma
 * trocando sinais de linhas e colunas inteiras, e essas trocas nunca desfazem a
 * ortogonalidade. Normalizar nao perde solucao, so corta a contagem de copias.
 */
public final class Busca {

    /** Acima disto a mascara nao cabe e a busca e recusada em vez de mentir. */
    public static final int ORDEM_MAXIMA = 20;

    private final int ordem;
    private long nosVisitados;

    public Busca(int ordem) {
        if (ordem < 1 || ordem > ORDEM_MAXIMA) {
            throw new IllegalArgumentException("ordem fora do alcance da busca: " + ordem);
        }
        this.ordem = ordem;
    }

    public long nosVisitados() {
        return nosVisitados;
    }

    /** Quantos tabuleiros a forca bruta cega olharia, sem poda nem normalizacao. */
    public static double tabuleirosSemPoda(int ordem) {
        return Math.pow(2, (double) ordem * ordem);
    }

    /** Quantos a normalizacao sozinha ja deixaria, ainda sem poda. */
    public static double tabuleirosNormalizados(int ordem) {
        return Math.pow(2, (double) (ordem - 1) * (ordem - 1));
    }

    /** A primeira matriz normalizada que aparecer, ou null se nao existir nenhuma. */
    public Matriz primeira() {
        List<Matriz> achadas = procurar(1);
        return achadas.isEmpty() ? null : achadas.get(0);
    }

    /** Todas as matrizes normalizadas de Hadamard desta ordem. */
    public List<Matriz> todas() {
        return procurar(Integer.MAX_VALUE);
    }

    /** Quantas matrizes normalizadas existem nesta ordem. */
    public int quantas() {
        return todas().size();
    }

    private List<Matriz> procurar(int quantasBastam) {
        nosVisitados = 0;
        List<Matriz> achadas = new ArrayList<>();
        int[] linhas = new int[ordem];
        // a primeira linha e sempre toda +1: a mascara e zero
        linhas[0] = 0;
        descer(linhas, 1, achadas, quantasBastam);
        return achadas;
    }

    private void descer(int[] linhas, int nivel, List<Matriz> achadas, int quantasBastam) {
        if (achadas.size() >= quantasBastam) {
            return;
        }
        if (nivel == ordem) {
            achadas.add(new Matriz(ordem, linhas));
            return;
        }
        int total = 1 << ordem;
        for (int candidata = 0; candidata < total; candidata++) {
            // a primeira coluna tambem e toda +1: o bit 0 fica sempre desligado
            if ((candidata & 1) != 0) {
                continue;
            }
            nosVisitados++;
            boolean serve = true;
            for (int anterior = 0; anterior < nivel && serve; anterior++) {
                if (!Matriz.ortogonais(linhas[anterior], candidata, ordem)) {
                    serve = false;
                }
            }
            if (!serve) {
                continue;
            }
            linhas[nivel] = candidata;
            descer(linhas, nivel + 1, achadas, quantasBastam);
            if (achadas.size() >= quantasBastam) {
                return;
            }
        }
    }

    /**
     * A regra que o teorema prevê: fora de 1 e 2, a ordem precisa ser multipla
     * de 4. Isto NAO e usado pela busca — serve para comparar a previsao com o
     * que a varredura encontrou.
     */
    public static boolean ordemPermitidaPeloTeorema(int ordem) {
        return ordem == 1 || ordem == 2 || ordem % 4 == 0;
    }
}
