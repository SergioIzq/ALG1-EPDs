package epd04;

import java.util.Random;

/**
 * Generador de "matrices mágicas": matrices de enteros aleatorios que
 * representan la energía mágica almacenada en un grimorio.
 *
 * Todos los métodos son estáticos: la clase no guarda más estado que el
 * generador de números aleatorios compartido.
 *
 * @author Sergio
 */
public final class GeneradorMatrices {

    /** Valor mínimo (incluido) de la energía de cada celda por defecto. */
    public static final int ENERGIA_MINIMA = 1;

    /** Valor máximo (incluido) de la energía de cada celda por defecto. */
    public static final int ENERGIA_MAXIMA = 9;

    /**
     * Generador de números pseudoaleatorios compartido por todos los métodos.
     * Se puede fijar su semilla con {@link #establecerSemilla(long)} para que
     * los experimentos sean reproducibles.
     */
    private static Random azar = new Random();

    /** Constructor privado: clase de utilidad que no se instancia. */
    private GeneradorMatrices() {
    }

    /**
     * Fija la semilla del generador aleatorio. Con la misma semilla se
     * obtienen siempre las mismas matrices, lo que permite repetir un
     * experimento en idénticas condiciones.
     *
     * @param semilla semilla del generador
     */
    public static void establecerSemilla(long semilla) {
        azar = new Random(semilla);
    }

    /**
     * Genera una matriz de dimensiones filas x columnas con valores enteros
     * aleatorios en el intervalo [ENERGIA_MINIMA, ENERGIA_MAXIMA].
     *
     * @param filas    número de filas (mayor que 0)
     * @param columnas número de columnas (mayor que 0)
     * @return matriz nueva con valores aleatorios
     * @throws IllegalArgumentException si alguna dimensión no es positiva
     */
    public static int[][] generarMatrizAleatoria(int filas, int columnas) {
        return generarMatrizAleatoria(filas, columnas, ENERGIA_MINIMA, ENERGIA_MAXIMA);
    }

    /**
     * Genera una matriz de dimensiones filas x columnas con valores enteros
     * aleatorios en el intervalo [minimo, maximo].
     *
     * @param filas    número de filas (mayor que 0)
     * @param columnas número de columnas (mayor que 0)
     * @param minimo   valor mínimo posible (incluido)
     * @param maximo   valor máximo posible (incluido)
     * @return matriz nueva con valores aleatorios
     * @throws IllegalArgumentException si alguna dimensión no es positiva o
     *                                  si minimo es mayor que maximo
     */
    public static int[][] generarMatrizAleatoria(int filas, int columnas, int minimo, int maximo) {
        // Validación de parámetros: una matriz necesita al menos una fila y una columna.
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException(
                    "Las dimensiones deben ser positivas: " + filas + "x" + columnas);
        }
        if (minimo > maximo) {
            throw new IllegalArgumentException(
                    "El mínimo (" + minimo + ") no puede ser mayor que el máximo (" + maximo + ")");
        }

        int[][] matriz = new int[filas][columnas];
        int amplitud = maximo - minimo + 1; // cantidad de valores posibles

        // Se recorre cada celda y se le asigna un valor aleatorio del intervalo.
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // nextInt(amplitud) devuelve un valor en [0, amplitud - 1];
                // al sumar el mínimo se desplaza al intervalo [minimo, maximo].
                matriz[i][j] = minimo + azar.nextInt(amplitud);
            }
        }
        return matriz;
    }
}
