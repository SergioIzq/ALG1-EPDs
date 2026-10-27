package epd04;

import epd04.interfaces.IResultadoExperimento;

/**
 * Resultado de un experimento: tiempos medidos al multiplicar dos matrices
 * cuadradas n x n con ambos algoritmos.
 *
 * Los tiempos están en nanosegundos. Se guardan el mínimo (medida principal,
 * la menos afectada por las perturbaciones del sistema operativo) y la media
 * de todas las repeticiones.
 *
 */
public final class ResultadoExperimento implements IResultadoExperimento {

    /** Dimensión de las matrices (n x n). */
    private final int dimension;

    /** Número de veces que se ejecutó cada algoritmo. */
    private final int repeticiones;

    /** Tiempo mínimo del algoritmo iterativo (ns). */
    private final long minIterativo;

    /** Tiempo medio del algoritmo iterativo (ns). */
    private final double mediaIterativo;

    /** Tiempo mínimo del algoritmo recursivo (ns). */
    private final long minRecursivo;

    /** Tiempo medio del algoritmo recursivo (ns). */
    private final double mediaRecursivo;

    /** true si ambos algoritmos devolvieron la misma matriz. */
    private final boolean resultadosIguales;

    /**
     * Crea el resultado de un experimento.
     *
     * @param dimension         dimensión de las matrices (n x n)
     * @param repeticiones      número de veces que se ejecutó cada algoritmo
     * @param minIterativo      tiempo mínimo del algoritmo iterativo (ns)
     * @param mediaIterativo    tiempo medio del algoritmo iterativo (ns)
     * @param minRecursivo      tiempo mínimo del algoritmo recursivo (ns)
     * @param mediaRecursivo    tiempo medio del algoritmo recursivo (ns)
     * @param resultadosIguales true si ambos algoritmos devolvieron la misma matriz
     */
    public ResultadoExperimento(int dimension, int repeticiones,
            long minIterativo, double mediaIterativo,
            long minRecursivo, double mediaRecursivo,
            boolean resultadosIguales) {
        this.dimension = dimension;
        this.repeticiones = repeticiones;
        this.minIterativo = minIterativo;
        this.mediaIterativo = mediaIterativo;
        this.minRecursivo = minRecursivo;
        this.mediaRecursivo = mediaRecursivo;
        this.resultadosIguales = resultadosIguales;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int getRepeticiones() {
        return repeticiones;
    }

    @Override
    public long getMinIterativo() {
        return minIterativo;
    }

    @Override
    public double getMediaIterativo() {
        return mediaIterativo;
    }

    @Override
    public long getMinRecursivo() {
        return minRecursivo;
    }

    @Override
    public double getMediaRecursivo() {
        return mediaRecursivo;
    }

    @Override
    public boolean isResultadosIguales() {
        return resultadosIguales;
    }

    /** Cociente entre el tiempo recursivo y el iterativo (con los mínimos). */
    @Override
    public double cocienteRecursivoIterativo() {
        return (double) minRecursivo / minIterativo;
    }
}
