package epd04;

/**
 * Resultado de un experimento: tiempos medidos al multiplicar dos matrices
 * cuadradas n x n con ambos algoritmos.
 *
 * Los tiempos están en nanosegundos. Se guardan el mínimo (medida principal,
 * la menos afectada por las perturbaciones del sistema operativo) y la media
 * de todas las repeticiones.
 *
 * Es un simple contenedor de datos inmutable: no tiene métodos, sus campos
 * son públicos y finales y se leen directamente.
 *
 */
public final class ResultadoExperimento {

    /** Dimensión de las matrices (n x n). */
    public final int dimension;

    /** Número de veces que se ejecutó cada algoritmo. */
    public final int repeticiones;

    /** Tiempo mínimo del algoritmo iterativo (ns). */
    public final long minIterativo;

    /** Tiempo medio del algoritmo iterativo (ns). */
    public final double mediaIterativo;

    /** Tiempo mínimo del algoritmo recursivo (ns). */
    public final long minRecursivo;

    /** Tiempo medio del algoritmo recursivo (ns). */
    public final double mediaRecursivo;

    /** true si ambos algoritmos devolvieron la misma matriz. */
    public final boolean resultadosIguales;

    /**
     * Crea el resultado de un experimento.
     *
     * @param dimension dimensión de las matrices (n x n)
     * @param repeticiones número de veces que se ejecutó cada algoritmo
     * @param minIterativo tiempo mínimo del algoritmo iterativo (ns)
     * @param mediaIterativo tiempo medio del algoritmo iterativo (ns)
     * @param minRecursivo tiempo mínimo del algoritmo recursivo (ns)
     * @param mediaRecursivo tiempo medio del algoritmo recursivo (ns)
     * @param resultadosIguales true si ambos algoritmos devolvieron la misma matriz
     */
    public ResultadoExperimento(
            int dimension,
            int repeticiones,
            long minIterativo,
            double mediaIterativo,
            long minRecursivo,
            double mediaRecursivo,
            boolean resultadosIguales) {
        this.dimension = dimension;
        this.repeticiones = repeticiones;
        this.minIterativo = minIterativo;
        this.mediaIterativo = mediaIterativo;
        this.minRecursivo = minRecursivo;
        this.mediaRecursivo = mediaRecursivo;
        this.resultadosIguales = resultadosIguales;
    }
}
