package epd04.interfaces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contrato del sistema de experimentación automática: medición de tiempos,
 * análisis empírico y exportación de resultados.
 *
 */
public interface IExperimentos {

    /** Ejecuta ambos algoritmos repetidamente para calentar la JVM antes de medir. */
    void calentarJVM();

    /**
     * Ejecuta todos los experimentos, uno por cada dimensión.
     *
     * @param dimensiones dimensiones a analizar
     * @return resultados ordenados por dimensión
     */
    List<IResultadoExperimento> ejecutarTodos(int[] dimensiones);

    /** Devuelve el valor acumulado en la variable de consumo. */
    long getConsumo();

    /** Muestra por consola la tabla de tiempos ordenada por dimensión. */
    void mostrarTabla(List<IResultadoExperimento> resultados);

    /** Relaciona las mediciones con la complejidad teórica O(n³). */
    void mostrarAnalisisEmpirico(List<IResultadoExperimento> resultados);

    /**
     * Pendiente de la recta de mínimos cuadrados de log(T) frente a log(n),
     * usando solo las dimensiones n >= nMinimo.
     *
     * @param iterativo true para el algoritmo iterativo, false para el recursivo
     */
    double pendienteLogLog(List<IResultadoExperimento> resultados, boolean iterativo, int nMinimo);

    /**
     * Constante c del ajuste por mínimos cuadrados T(n) ≈ c·n³ (en ns).
     *
     * @param iterativo true para el algoritmo iterativo, false para el recursivo
     */
    double constanteAjusteCubico(List<IResultadoExperimento> resultados, boolean iterativo);

    /**
     * Guarda los resultados en un fichero CSV.
     *
     * @param resultados resultados de los experimentos
     * @param fichero ruta del CSV de salida
     * @throws IOException si no se puede escribir el fichero
     */
    void guardarCSV(List<IResultadoExperimento> resultados, Path fichero) throws IOException;
}
