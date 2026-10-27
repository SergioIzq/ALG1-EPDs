package epd04.interfaces;

/**
 * Contrato del resultado de un experimento: tiempos medidos al multiplicar dos
 * matrices cuadradas n x n con ambos algoritmos (en nanosegundos).
 *
 */
public interface IResultadoExperimento {

    /** Dimensión de las matrices (n x n). */
    int getDimension();

    /** Número de veces que se ejecutó cada algoritmo. */
    int getRepeticiones();

    /** Tiempo mínimo del algoritmo iterativo (ns). */
    long getMinIterativo();

    /** Tiempo medio del algoritmo iterativo (ns). */
    double getMediaIterativo();

    /** Tiempo mínimo del algoritmo recursivo (ns). */
    long getMinRecursivo();

    /** Tiempo medio del algoritmo recursivo (ns). */
    double getMediaRecursivo();

    /** true si ambos algoritmos devolvieron la misma matriz. */
    boolean isResultadosIguales();

    /** Cociente entre el tiempo recursivo y el iterativo (con los mínimos). */
    double cocienteRecursivoIterativo();
}
