package br.com.conde.hadamard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os numeros do README, presos. Todos sairam do {@link Medidor}. */
class MedidasTest {

    @Test
    @DisplayName("a busca acha matriz em 1, 2, 4, 8 e 12, e em mais nenhuma ate 12")
    void ondeExiste() {
        for (int n = 1; n <= 12; n++) {
            boolean existe = new Busca(n).primeira() != null;
            boolean esperado = n == 1 || n == 2 || n == 4 || n == 8 || n == 12;
            assertThat(existe).as("ordem %d", n).isEqualTo(esperado);
        }
    }

    @Test
    @DisplayName("a busca reproduz o teorema sem conhece-lo")
    void aBuscaReproduzOTeorema() {
        for (int n = 1; n <= 12; n++) {
            boolean existe = new Busca(n).primeira() != null;
            assertThat(existe)
                    .as("ordem %d: busca diz %s, teorema diz %s",
                            n, existe, Busca.ordemPermitidaPeloTeorema(n))
                    .isEqualTo(Busca.ordemPermitidaPeloTeorema(n));
        }
    }

    @Test
    @DisplayName("as ordens impossiveis sao VAZIAS, nao apenas dificeis")
    void ordensImpossiveisSaoVazias() {
        for (int n : new int[] {3, 5, 6, 7, 9, 10, 11}) {
            assertThat(new Busca(n).todas()).as("ordem %d", n).isEmpty();
        }
    }

    @Test
    @DisplayName("6 matrizes normalizadas na ordem 4, e 151.200 na ordem 8")
    void quantasExistem() {
        assertThat(new Busca(1).quantas()).isEqualTo(1);
        assertThat(new Busca(2).quantas()).isEqualTo(1);
        assertThat(new Busca(4).quantas()).isEqualTo(6);
        assertThat(new Busca(8).quantas()).isEqualTo(151_200);
    }

    @Test
    @DisplayName("a poda visita 80 nos na ordem 4 e 33.067.008 na ordem 8")
    void oQueAPodaEconomiza() {
        Busca quatro = new Busca(4);
        quatro.todas();
        assertThat(quatro.nosVisitados()).isEqualTo(80);

        Busca oito = new Busca(8);
        oito.todas();
        assertThat(oito.nosVisitados()).isEqualTo(33_067_008L);

        // contra 2^64 tabuleiros da forca bruta cega
        assertThat(Busca.tabuleirosSemPoda(8)).isCloseTo(1.8447e19, org.assertj.core.data.Offset.offset(1e15));
        assertThat(Busca.tabuleirosSemPoda(8) / oito.nosVisitados()).isGreaterThan(5e11);
    }

    @Test
    @DisplayName("na ordem 16 a primeira matriz aparece em 262.151 nos")
    void aOrdem16() {
        Busca busca = new Busca(Medidor.ORDEM_FUNDO);
        Matriz primeira = busca.primeira();
        assertThat(primeira).isNotNull();
        assertThat(primeira.ehHadamard()).isTrue();
        assertThat(primeira.colunasOrtogonais()).isTrue();
        assertThat(busca.nosVisitados()).isEqualTo(262_151L);
        // a forca bruta cega olharia 2^256
        assertThat(Busca.tabuleirosSemPoda(16)).isGreaterThan(1e76);
    }

    @Test
    @DisplayName("os dois caminhos de ortogonalidade concordam em 4.233.637 pares")
    void juiz() {
        long pares = 0;
        for (int n : new int[] {1, 2, 4, 8}) {
            for (Matriz m : new Busca(n).todas()) {
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        assertThat(Matriz.ortogonais(m.linha(i), m.linha(j), n))
                                .as("ordem %d", n)
                                .isEqualTo(m.produtoInterno(i, j) == 0);
                        pares++;
                    }
                }
            }
        }
        assertThat(pares).isEqualTo(4_233_637L);
    }

    @Test
    @DisplayName("todas as 151.208 matrizes achadas tem colunas ortogonais")
    void todasComColunasOrtogonais() {
        int quantas = 0;
        for (int n : new int[] {1, 2, 4, 8}) {
            for (Matriz m : new Busca(n).todas()) {
                assertThat(m.colunasOrtogonais()).as("ordem %d", n).isTrue();
                quantas++;
            }
        }
        assertThat(quantas).isEqualTo(151_208);
        assertThat(quantas).isEqualTo(1 + 1 + 6 + 151_200);
    }
}
