package epd04.interfaces;

/**
 * Contrato del generador de "matrices mágicas": matrices de enteros aleatorios.
 *
 */
public interface IGeneradorMatrices {

    /**
     * Fija la semilla del generador aleatorio para poder repetir un
     * experimento en idénticas condiciones.
     *
     * @param semilla semilla del generador
     */
    void establecerSemilla(long semilla);

    /**
     * Genera una matriz filas x columnas con valores aleatorios en el
     * intervalo de energía por defecto.
     *
     * @param filas número de filas (mayor que 0)
     * @param columnas número de columnas (mayor que 0)
     * @return matriz nueva con valores aleatorios
     * @throws IllegalArgumentException si alguna dimensión no es positiva
     */
    int[][] generarMatrizAleatoria(int filas, int columnas);

    /**
     * Genera una matriz filas x columnas con valores aleatorios en el
     * intervalo [minimo, maximo].
     *
     * @param filas número de filas (mayor que 0)
     * @param columnas número de columnas (mayor que 0)
     * @param minimo valor mínimo posible (incluido)
     * @param maximo valor máximo posible (incluido)
     * @return matriz nueva con valores aleatorios
     * @throws IllegalArgumentException si alguna dimensión no es positiva o
     * si minimo es mayor que maximo
     */
    int[][] generarMatrizAleatoria(int filas, int columnas, int minimo, int maximo);
}
