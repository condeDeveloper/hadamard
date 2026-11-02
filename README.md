# hadamard

Monte uma tabela `n × n` só com `+1` e `-1`, de modo que **quaisquer duas linhas
discordem em exatamente metade das colunas**. Para `n = 4` dá:

```
++++
+-+-
++--
+--+
```

Para `n = 3` não dá. Nem para 5, 6, 7, 9, 10 ou 11. Dá para 1, 2, e — conjectura-se —
todo múltiplo de 4. Este repositório **procura, não supõe**: monta as matrizes linha a
linha e, quando não existe nenhuma, a busca termina de mãos vazias tendo olhado todas as
possibilidades.

```bash
mvn -B -ntp verify                              # 15 testes
mvn -B -ntp exec:java -Dexec.args="ordens"      # para que ordens existe
mvn -B -ntp exec:java -Dexec.args="poda"        # o que a poda economiza
mvn -B -ntp exec:java -Dexec.args="sylvester"   # a construção que dobra a ordem
mvn -B -ntp exec:java -Dexec.args="juiz"        # as três conferências
mvn -B -ntp exec:java -Dexec.args="fundo"       # ordem 16 (job próprio)
```

Java 21, sem dependência fora de JUnit e AssertJ.

## A busca não conhece o teorema

```
ordem   existe?   normalizadas    o teorema previa   bate?
1       sim       1               pode existir       sim
2       sim       1               pode existir       sim
3       não       0               não pode           sim
4       sim       6               pode existir       sim
5       não       0               não pode           sim
6       não       0               não pode           sim
7       não       0               não pode           sim
8       sim       151200          pode existir       sim
9       não       0               não pode           sim
10      não       0               não pode           sim
11      não       0               não pode           sim
12      sim       (não contado)   pode existir       sim
```

A coluna "não pode" vem do teorema — fora de 1 e 2, a ordem precisa ser múltipla de 4. A
coluna "existe?" vem da busca, que não sabe disso e simplesmente tenta montar.

As sete ordens impossíveis não são "não achei": são **vazias**. A busca percorre toda a
árvore de montagem e volta sem nada, e um teste afirma exatamente isso para 3, 5, 6, 7, 9,
10 e 11.

## O que a poda economiza

A ortogonalidade é verificada a cada linha nova, não no fim. Uma linha que já conflita com
uma anterior nunca vira boa depois, então o ramo morre ali.

```
ordem   força bruta cega   só normalizando   com poda      redução
4       6,554e+04          5,120e+02         80            8,192e+02 vezes
8       1,845e+19          5,629e+14         33.067.008    5,579e+11 vezes
```

E na ordem 16, onde a força bruta cega olharia **1,158 × 10⁷⁷ tabuleiros** — mais do que
há átomos no universo observável —, a primeira matriz aparece depois de **262.151 nós**,
instantaneamente.

Duas economias diferentes estão em jogo. A **normalização** (fixar a primeira linha e a
primeira coluna como `+1`) corta de `2^(n²)` para `2^((n-1)²)` sem perder solução, porque
trocar o sinal de uma linha ou coluna inteira nunca desfaz a ortogonalidade. A **poda** faz
o resto, e é ela que faz a diferença entre impossível e instantâneo.

## O truque de bits

Guardando cada linha como uma máscara — bit ligado quer dizer `-1` —, a pergunta "essas
duas linhas são ortogonais?" vira:

```java
Integer.bitCount(a ^ b) * 2 == ordem
```

O `xor` acende os bits onde as linhas diferem, e elas são ortogonais exatamente quando
diferem em metade das posições. Isso substitui um laço de `n` multiplicações e somas por
duas instruções.

Como é exatamente o tipo de esperteza que dá errado em silêncio, o produto interno somado
na marra continua no projeto e é conferido contra o truque:

```
pares de linhas conferidos nos dois caminhos : 4.233.637, todos iguais
matrizes em que as colunas também são ortogonais : 151.208 de 151.208
construções de Sylvester que deram Hadamard : 5 de 5
```

A segunda linha é um teorema virando medida: a definição só exige ortogonalidade das
**linhas**, e as colunas saem de graça — em todas as 151.208 matrizes encontradas, sem
exceção.

## A construção de Sylvester

A partir de uma matriz `H`, monta-se uma de ordem dobrada:

```
[ H   H ]
[ H  -H ]
```

Começando de `[1]`, isso dá matrizes de ordem 1, 2, 4, 8, 16, e todas passam no teste. É
o motivo de as potências de 2 serem fáceis — e de o caso difícil ser a ordem 12, que não é
potência de 2 e precisa de outra construção.

## O que as medidas me corrigiram

**Quis enumerar todas na ordem 12 e travei a máquina.** A tabela original contava as
matrizes normalizadas de toda ordem até 12. A ordem 8 já tem **151.200**, e a 12 tem
tantas que a enumeração não terminou em dez minutos — tive que matar o processo. Hoje a
tabela separa duas perguntas: **existe?**, que a busca responde parando na primeira, e
**quantas?**, que só é contado até a ordem 8. A coluna diz `(não contado)` em vez de
inventar um número ou deixar a impressão de que a contagem foi feita.

**"Não achei" e "não existe" são coisas diferentes.** Uma busca com poda que volta vazia
só prova ausência se a poda for correta — e a poda aqui é correta porque descarta apenas
ramos que já violam a definição. É por isso que o teste das ordens impossíveis usa
`todas()` e exige lista vazia, em vez de usar `primeira()` e aceitar `null`: o primeiro
percorre a árvore inteira.

**A ordem 16 me surpreendeu pelo lado contrário.** Esperava que ela fosse o caso caro do
projeto, com 10⁷⁷ tabuleiros no espaço de busca. É o mais rápido de todos: a estrutura de
Sylvester está tão presente no espaço que a primeira matriz aparece quase imediatamente.
O caso difícil é **contar**, não **achar**.

## Quatro achados

1. A busca, que não conhece o teorema, **reproduziu a previsão dele em todas as doze
   ordens**: acha matriz em 1, 2, 4, 8 e 12, e volta provadamente vazia em 3, 5, 6, 7, 9,
   10 e 11.
2. Na ordem 16, a força bruta cega olharia **1,158 × 10⁷⁷ tabuleiros**; a poda acha a
   primeira matriz em **262.151 nós**.
3. **As colunas são ortogonais em todas as 151.208 matrizes** encontradas, embora a
   definição só exija isso das linhas.
4. Achar é fácil, contar é caro: a ordem 8 tem **151.200** matrizes normalizadas, e a
   ordem 12 tem tantas que a enumeração não termina — mas a primeira delas aparece em
   menos de um segundo.
