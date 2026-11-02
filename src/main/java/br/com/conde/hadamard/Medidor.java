package br.com.conde.hadamard;

import java.util.List;

/**
 * Roda as tabelas do README.
 *
 * <pre>
 * mvn -B -ntp exec:java -Dexec.args="ordens"
 * mvn -B -ntp exec:java -Dexec.args="poda"
 * mvn -B -ntp exec:java -Dexec.args="sylvester"
 * mvn -B -ntp exec:java -Dexec.args="juiz"
 * mvn -B -ntp exec:java -Dexec.args="fundo"     (ordem 16, job proprio)
 * </pre>
 */
public final class Medidor {

    public static final int ORDEM = 12;

    /** Ate onde vale enumerar TODAS: acima disso a contagem explode. */
    public static final int ORDEM_CONTAGEM = 8;
    public static final int ORDEM_FUNDO = 16;

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";
        switch (comando) {
            case "ordens" -> ordens();
            case "poda" -> poda();
            case "sylvester" -> sylvester();
            case "juiz" -> juiz();
            case "fundo" -> fundo();
            case "tudo" -> {
                ordens();
                poda();
                sylvester();
                juiz();
            }
            default -> {
                System.out.println("comando desconhecido: " + comando);
                System.exit(2);
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("-".repeat(texto.length()));
    }

    private static void ordens() {
        titulo("Para que ordens existe matriz de Hadamard, ate " + ORDEM);
        System.out.printf("%-8s %-12s %-16s %-16s %s%n",
                "ordem", "existe?", "normalizadas", "o teorema previa", "bate?");
        for (int n = 1; n <= ORDEM; n++) {
            Busca busca = new Busca(n);
            boolean existe = busca.primeira() != null;
            boolean previsto = Busca.ordemPermitidaPeloTeorema(n);
            // contar TODAS so vale a pena nas ordens pequenas: na 12 o numero
            // de matrizes normalizadas passa de milhoes e a enumeracao nao
            // termina em tempo de CI.
            String quantas = n <= ORDEM_CONTAGEM
                    ? String.valueOf(new Busca(n).quantas())
                    : "(nao contado)";
            System.out.printf("%-8d %-12s %-16s %-16s %s%n",
                    n, existe ? "sim" : "nao", quantas,
                    previsto ? "pode existir" : "nao pode",
                    previsto == existe ? "sim" : "NAO BATE");
        }
        System.out.println();
        System.out.println("A busca nao conhece o teorema: ela so tenta montar e falha.");
    }

    private static void poda() {
        titulo("O que a poda e a normalizacao economizam");
        System.out.printf("%-8s %-20s %-20s %-16s %s%n",
                "ordem", "forca bruta cega", "so normalizando", "com poda", "reducao");
        for (int n : new int[] {4, 8}) {
            Busca busca = new Busca(n);
            busca.todas();
            double cega = Busca.tabuleirosSemPoda(n);
            double normalizada = Busca.tabuleirosNormalizados(n);
            long comPoda = busca.nosVisitados();
            System.out.printf("%-8d %-20.3e %-20.3e %-16d %.3e vezes menos%n",
                    n, cega, normalizada, comPoda, cega / comPoda);
        }
    }

    private static void sylvester() {
        titulo("A construcao de Sylvester");
        for (int k = 0; k <= 4; k++) {
            Matriz m = Matriz.deSylvester(k);
            System.out.printf("ordem %-4d  Hadamard: %-6s  normalizada: %-6s  colunas ortogonais: %s%n",
                    m.ordem(), m.ehHadamard(), m.normalizada(), m.colunasOrtogonais());
        }
        System.out.println();
        System.out.println("A de ordem 4:");
        System.out.print(Matriz.deSylvester(2));
        System.out.println();
        System.out.println("A de ordem 8:");
        System.out.print(Matriz.deSylvester(3));
    }

    private static void juiz() {
        titulo("As tres conferencias");

        int ortogonalidadePorBits = 0;
        int colunasConferidas = 0;
        for (int n : new int[] {1, 2, 4, 8}) {
            List<Matriz> todas = new Busca(n).todas();
            for (Matriz m : todas) {
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        // o truque de bits contra a soma na marra
                        boolean porBits = Matriz.ortogonais(m.linha(i), m.linha(j), n);
                        boolean porSoma = m.produtoInterno(i, j) == 0;
                        if (porBits != porSoma) {
                            System.out.printf("DISCORDARAM na ordem %d, linhas %d e %d%n", n, i, j);
                            System.exit(1);
                        }
                        ortogonalidadePorBits++;
                    }
                }
                if (!m.colunasOrtogonais()) {
                    System.out.printf("COLUNAS NAO ORTOGONAIS na ordem %d%n", n);
                    System.exit(1);
                }
                colunasConferidas++;
            }
        }
        System.out.printf("pares de linhas conferidos nos dois caminhos: %d, todos iguais%n",
                ortogonalidadePorBits);
        System.out.printf("matrizes em que as colunas tambem sao ortogonais: %d de %d%n",
                colunasConferidas, colunasConferidas);

        int sylvesterConferidas = 0;
        for (int k = 0; k <= 4; k++) {
            if (Matriz.deSylvester(k).ehHadamard()) {
                sylvesterConferidas++;
            }
        }
        System.out.printf("construcoes de Sylvester que deram Hadamard: %d de 5%n", sylvesterConferidas);
    }

    private static void fundo() {
        titulo("Busca na ordem " + ORDEM_FUNDO);
        long comeco = System.nanoTime();
        Busca busca = new Busca(ORDEM_FUNDO);
        Matriz primeira = busca.primeira();
        System.out.printf("achou matriz : %s%n", primeira != null);
        System.out.printf("nos visitados ate achar a primeira: %d%n", busca.nosVisitados());
        if (primeira != null) {
            System.out.printf("e de Hadamard: %s%n", primeira.ehHadamard());
            System.out.printf("colunas ortogonais: %s%n", primeira.colunasOrtogonais());
            System.out.printf("%nforca bruta cega olharia %.3e tabuleiros%n",
                    Busca.tabuleirosSemPoda(ORDEM_FUNDO));
        }
        System.out.printf("%ntempo: %.1f s%n", (System.nanoTime() - comeco) / 1e9);

        if (primeira == null || !primeira.ehHadamard()) {
            System.out.println("MEDIDA MUDOU: a ordem 16 deveria ter matriz de Hadamard");
            System.exit(1);
        }
        System.out.println("a medida do README bateu");
    }
}
