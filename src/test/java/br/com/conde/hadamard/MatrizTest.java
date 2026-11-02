package br.com.conde.hadamard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatrizTest {

    @Test
    @DisplayName("o bit ligado quer dizer menos um")
    void bitLigadoEhMenosUm() {
        Matriz m = new Matriz(2, new int[] {0b00, 0b10});
        assertThat(m.valor(0, 0)).isEqualTo(1);
        assertThat(m.valor(0, 1)).isEqualTo(1);
        assertThat(m.valor(1, 0)).isEqualTo(1);
        assertThat(m.valor(1, 1)).isEqualTo(-1);
    }

    @Test
    @DisplayName("a matriz de ordem 2 e de Hadamard, e a de ordem 1 tambem")
    void asPequenas() {
        assertThat(Matriz.ordemUm().ehHadamard()).isTrue();
        Matriz dois = new Matriz(2, new int[] {0b00, 0b10});
        assertThat(dois.ehHadamard()).isTrue();
        assertThat(dois.normalizada()).isTrue();
    }

    @Test
    @DisplayName("o truque de bits concorda com a soma na marra")
    void bitsConcordamComASoma() {
        for (int k = 0; k <= 4; k++) {
            Matriz m = Matriz.deSylvester(k);
            int n = m.ordem();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        assertThat(m.produtoInterno(i, i)).as("ordem %d, diagonal", n).isEqualTo(n);
                        continue;
                    }
                    boolean porBits = Matriz.ortogonais(m.linha(i), m.linha(j), n);
                    boolean porSoma = m.produtoInterno(i, j) == 0;
                    assertThat(porBits).as("ordem %d, linhas %d e %d", n, i, j).isEqualTo(porSoma);
                }
            }
        }
    }

    @Test
    @DisplayName("Sylvester dobra a ordem e continua de Hadamard")
    void sylvester() {
        for (int k = 0; k <= 4; k++) {
            Matriz m = Matriz.deSylvester(k);
            assertThat(m.ordem()).isEqualTo(1 << k);
            assertThat(m.ehHadamard()).as("ordem %d", m.ordem()).isTrue();
            assertThat(m.normalizada()).as("ordem %d", m.ordem()).isTrue();
            assertThat(m.colunasOrtogonais()).as("ordem %d", m.ordem()).isTrue();
        }
    }

    @Test
    @DisplayName("se as linhas sao ortogonais, as colunas tambem sao")
    void colunasSeguemAsLinhas() {
        for (int n : new int[] {1, 2, 4, 8}) {
            for (Matriz m : new Busca(n).todas()) {
                assertThat(m.ehHadamard()).isTrue();
                assertThat(m.colunasOrtogonais()).as("ordem %d", n).isTrue();
            }
        }
    }

    @Test
    @DisplayName("a matriz de ordem 4 desenhada tem os sinais certos")
    void desenhoDaOrdem4() {
        assertThat(Matriz.deSylvester(2).toString()).isEqualTo(
                "++++\n" + "+-+-\n" + "++--\n" + "+--+\n");
    }

    @Test
    @DisplayName("entrada sem sentido e recusada na porta")
    void recusaEntradaInvalida() {
        assertThatThrownBy(() -> new Matriz(0, new int[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Matriz(3, new int[] {0, 0}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
