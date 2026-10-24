package epd04;

/**
 * Resultado de un experimento: tiempos medidos al multiplicar dos matrices
 * cuadradas n x n con ambos algoritmos.
 *
 * Los tiempos están en nanosegundos. Se guardan el mínimo (medida principal,
 * la menos afectada por las perturbaciones del sistema operativo) y la media
 * de todas las repeticiones.
 *
 * @param n            dimensión de las matrices (n x n)
 * @param repeticiones número de veces que se ejecutó cada algoritmo
 * @param minIterativo tiempo mínimo del algoritmo iterativo (ns)
 * @param mediaIterativo tiempo medio del algoritmo iterativo (ns)
 * @param minRecursivo tiempo mínimo del algoritmo recursivo (ns)
 * @param mediaRecursivo tiempo medio del algoritmo recursivo (ns)
 * @param resultadosIguales true si ambos algoritmos devolvieron la misma matriz
 *
 * @author Sergio
 */
public record ResultadoExperimento(
        int n,
        int repeticiones,
        long minIterativo,
        double mediaIterativo,
        long minRecursivo,
        double mediaRecursivo,
        boolean resultadosIguales) {

    /** Cociente entre el tiempo recursivo y el iterativo (con los mínimos). */
    public double cocienteRecursivoIterativo() {
        return (double) minRecursivo / minIterativo;
    }
}
