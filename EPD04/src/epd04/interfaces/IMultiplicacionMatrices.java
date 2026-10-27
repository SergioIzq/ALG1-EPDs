package epd04.interfaces;

/**
 * Contrato de los hechizos de combinación de energía: multiplicación de
 * matrices con un algoritmo iterativo y con uno recursivo.
 *
 */
public interface IMultiplicacionMatrices {

    /**
     * Comprueba si dos matrices se pueden multiplicar (columnas de A = filas
     * de B, ambas rectangulares y no vacías).
     *
     * @param a primera matriz
     * @param b segunda matriz
     * @return true si el producto A · B está definido
     */
    boolean sonCompatibles(int[][] a, int[][] b);

    /**
     * Multiplica A (m x n) por B (n x p) con el algoritmo iterativo.
     *
     * @param a matriz izquierda (m x n)
     * @param b matriz derecha (n x p)
     * @return matriz producto C (m x p)
     * @throws IllegalArgumentException si las dimensiones no son compatibles
     */
    int[][] multiplicarIterativo(int[][] a, int[][] b);

    /**
     * Multiplica A (m x n) por B (n x p) con el algoritmo recursivo de divide
     * y vencerás.
     *
     * @param a matriz izquierda (m x n)
     * @param b matriz derecha (n x p)
     * @return matriz producto C (m x p)
     * @throws IllegalArgumentException si las dimensiones no son compatibles
     */
    int[][] multiplicarRecursivo(int[][] a, int[][] b);

    /**
     * Compara dos matrices elemento a elemento.
     *
     * @return true si tienen las mismas dimensiones y los mismos valores
     */
    boolean sonIguales(int[][] x, int[][] y);

    /**
     * Cuenta las llamadas que realiza el algoritmo recursivo para un producto
     * A (m x n) · B (n x p).
     *
     * @return número total de llamadas (la inicial incluida)
     */
    long contarLlamadasRecursivas(int m, int n, int p);
}
