package epd04;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BinaryOperator;

/**
 * Sistema de experimentación automática.
 *
 * Para cada dimensión n de la lista de experimentos:
 *  1. Se generan en memoria dos matrices aleatorias n x n (A y B).
 *  2. Se multiplican varias veces con cada algoritmo midiendo el tiempo con
 *     System.nanoTime() y se registran el mínimo y la media.
 *  3. Se comprueba que ambos algoritmos devuelven la misma matriz.
 *
 * Los resultados se muestran por consola y se guardan además en un fichero
 * CSV en la carpeta "resultados".
 *
 * Metodología de medida (EPD 2):
 *  - Fase de calentamiento previa para que el compilador JIT de la JVM
 *    optimice ambos métodos antes de medir.
 *  - Todas las repeticiones usan exactamente las mismas matrices de entrada.
 *  - Se toma como medida principal el tiempo mínimo, que es el menos afectado
 *    por las interrupciones del sistema operativo o del recolector de basura.
 *  - Las ejecuciones de ambos algoritmos se alternan para que cualquier
 *    perturbación temporal afecte por igual a los dos.
 *
 */
public final class Experimentos {

    /** Dimensiones n de las matrices cuadradas n x n que se analizan. */
    public static final int[] DIMENSIONES = {10, 25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 600, 700, 800};

    /** Factor para convertir nanosegundos a milisegundos. */
    private static final double NS_POR_MS = 1_000_000.0;

    /**
     * Variable de consumo. Cada resultado calculado durante el calentamiento
     * y las mediciones "se consume" sumando uno de sus valores aquí.
     *
     * Motivo: el compilador JIT de la JVM elimina el código cuyo resultado
     * nunca se usa (eliminación de código muerto). Si el producto de matrices
     * no se usara, el JIT podría descartar parte del cálculo y estaríamos
     * midiendo menos trabajo del real. Al acumular los resultados en esta
     * variable (y mostrarla al final del programa) el cálculo es siempre
     * necesario y las mediciones son fiables.
     */
    private static long consumo = 0;

    /** Clase de utilidad con métodos estáticos: no se instancia. */
    private Experimentos() {
    }

    // CALENTAMIENTO DE LA JVM

    /**
     * Ejecuta ambos algoritmos repetidamente sobre matrices pequeñas para que
     * la JVM compile y optimice su código antes de empezar a medir. Sin esta
     * fase, las primeras mediciones incluirían el tiempo de interpretación y
     * compilación, y no serían comparables con las demás.
     */
    public static void calentarJVM() {
        int[][] a = GeneradorMatrices.generarMatrizAleatoria(64, 64);
        int[][] b = GeneradorMatrices.generarMatrizAleatoria(64, 64);
        for (int i = 0; i < 300; i++) {
            consumir(MultiplicacionMatrices.multiplicarIterativo(a, b));
            consumir(MultiplicacionMatrices.multiplicarRecursivo(a, b));
        }
    }

    // EJECUCIÓN DE EXPERIMENTOS

    /**
     * Ejecuta todos los experimentos, uno por cada dimensión, y muestra el
     * progreso por consola.
     *
     * @param dimensiones dimensiones a analizar
     * @return resultados ordenados por dimensión
     */
    public static List<ResultadoExperimento> ejecutarTodos(int[] dimensiones) {
        List<ResultadoExperimento> resultados = new ArrayList<>();
        for (int n : dimensiones) {
            ResultadoExperimento r = ejecutarExperimento(n);
            resultados.add(r);
            System.out.printf(Locale.ROOT,
                    "   n = %4d  (%2d repeticiones)  iterativo: %11.4f ms   recursivo: %11.4f ms   %s%n",
                    n, r.repeticiones,
                    r.minIterativo / NS_POR_MS, r.minRecursivo / NS_POR_MS,
                    r.resultadosIguales ? "[resultados iguales]" : "[ERROR: resultados distintos]");
        }
        return resultados;
    }

    /**
     * Ejecuta el experimento para matrices n x n: genera A y B aleatoriamente
     * y mide ambos algoritmos sobre ellas.
     */
    private static ResultadoExperimento ejecutarExperimento(int n) {
        // Generación de las matrices aleatorias (no forma parte del tiempo medido).
        int[][] a = GeneradorMatrices.generarMatrizAleatoria(n, n);
        int[][] b = GeneradorMatrices.generarMatrizAleatoria(n, n);

        int repeticiones = repeticionesPara(n);
        long[] tiemposIter = new long[repeticiones];
        long[] tiemposRec = new long[repeticiones];
        int[][] resultadoIter = null;
        int[][] resultadoRec = null;

        // Se libera memoria antes de medir para reducir la probabilidad de que
        // el recolector de basura interrumpa una medición.
        System.gc();

        for (int r = 0; r < repeticiones; r++) {
            // Se alternan los algoritmos en cada repetición.
            resultadoIter = medir(MultiplicacionMatrices::multiplicarIterativo, a, b, tiemposIter, r);
            resultadoRec = medir(MultiplicacionMatrices::multiplicarRecursivo, a, b, tiemposRec, r);
        }

        boolean iguales = MultiplicacionMatrices.sonIguales(resultadoIter, resultadoRec);
        return new ResultadoExperimento(n, repeticiones,
                minimo(tiemposIter), media(tiemposIter),
                minimo(tiemposRec), media(tiemposRec),
                iguales);
    }

    /**
     * Ejecuta una vez el algoritmo indicado y guarda el tiempo empleado en
     * tiempos[posicion]. Solo se mide la llamada al algoritmo.
     *
     * @return la matriz producto calculada
     */
    private static int[][] medir(BinaryOperator<int[][]> algoritmo, int[][] a, int[][] b,
            long[] tiempos, int posicion) {
        long inicio = System.nanoTime();
        int[][] c = algoritmo.apply(a, b);
        long fin = System.nanoTime();
        tiempos[posicion] = fin - inicio;
        consumir(c);
        return c;
    }

    /**
     * Número de repeticiones según la dimensión: las matrices pequeñas se
     * multiplican en microsegundos y necesitan más repeticiones para obtener
     * una medida estable; las grandes tardan más y con menos basta.
     */
    private static int repeticionesPara(int n) {
        if (n <= 50) {
            return 50;
        } else if (n <= 150) {
            return 20;
        } else if (n <= 300) {
            return 10;
        } else if (n <= 500) {
            return 5;
        }
        return 3;
    }

    /** Acumula un valor del resultado en la variable de consumo (ver consumo). */
    private static void consumir(int[][] c) {
        consumo += c[c.length - 1][c[0].length - 1];
    }

    /** Devuelve el valor acumulado en la variable de consumo. */
    public static long getConsumo() {
        return consumo;
    }

    private static long minimo(long[] valores) {
        long min = Long.MAX_VALUE;
        for (long v : valores) {
            min = Math.min(min, v);
        }
        return min;
    }

    private static double media(long[] valores) {
        double suma = 0;
        for (long v : valores) {
            suma += v;
        }
        return suma / valores.length;
    }

    /** Cociente entre el tiempo recursivo y el iterativo (con los mínimos). */
    public static double cocienteRecursivoIterativo(ResultadoExperimento r) {
        return (double) r.minRecursivo / r.minIterativo;
    }

    // PRESENTACIÓN DE RESULTADOS POR CONSOLA

    /**
     * Muestra por consola la tabla de tiempos ordenada por dimensión.
     */
    public static void mostrarTabla(List<ResultadoExperimento> resultados) {
        String separador = "   " + "-".repeat(108);
        System.out.println(separador);
        System.out.printf("   %6s | %5s | %15s | %15s | %15s | %15s | %9s | %8s%n",
                "n", "reps", "Iter. mín (ms)", "Iter. media", "Rec. mín (ms)", "Rec. media",
                "Rec/Iter", "Correcto");
        System.out.println(separador);
        for (ResultadoExperimento r : resultados) {
            System.out.printf(Locale.ROOT, "   %6d | %5d | %15.4f | %15.4f | %15.4f | %15.4f | %9.2f | %8s%n",
                    r.dimension, r.repeticiones,
                    r.minIterativo / NS_POR_MS, r.mediaIterativo / NS_POR_MS,
                    r.minRecursivo / NS_POR_MS, r.mediaRecursivo / NS_POR_MS,
                    cocienteRecursivoIterativo(r),
                    r.resultadosIguales ? "sí" : "NO");
        }
        System.out.println(separador);
    }

    /**
     * Relaciona las mediciones con la complejidad teórica O(n³):
     *  - T(n)/n³ debe tender a una constante para cada algoritmo.
     *  - El exponente empírico entre dos dimensiones consecutivas,
     *    log(T2/T1) / log(n2/n1), debe aproximarse a 3.
     *  - La pendiente de la recta de regresión en escala log-log estima el
     *    exponente global. Se calcula con n >= 100 y con n >= 300 (cuando las
     *    matrices ya no caben en la caché L2 y el comportamiento es estable).
     */
    public static void mostrarAnalisisEmpirico(List<ResultadoExperimento> resultados) {
        String separador = "   " + "-".repeat(78);
        System.out.println(separador);
        System.out.printf("   %6s | %16s | %16s | %14s | %14s%n",
                "n", "Iter. T/n³ (ns)", "Rec. T/n³ (ns)", "Exp. iterat.", "Exp. recursivo");
        System.out.println(separador);
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoExperimento r = resultados.get(i);
            double n3 = Math.pow(r.dimension, 3);
            String expIter = "-";
            String expRec = "-";
            if (i > 0) {
                ResultadoExperimento ant = resultados.get(i - 1);
                double logN = Math.log((double) r.dimension / ant.dimension);
                expIter = String.format(Locale.ROOT, "%.2f",
                        Math.log((double) r.minIterativo / ant.minIterativo) / logN);
                expRec = String.format(Locale.ROOT, "%.2f",
                        Math.log((double) r.minRecursivo / ant.minRecursivo) / logN);
            }
            System.out.printf(Locale.ROOT, "   %6d | %16.3f | %16.3f | %14s | %14s%n",
                    r.dimension, r.minIterativo / n3, r.minRecursivo / n3, expIter, expRec);
        }
        System.out.println(separador);
        for (int nMinimo : new int[]{100, 300}) {
            System.out.printf(Locale.ROOT,
                    "   Pendiente log-log (n >= %d):  iterativo = %.2f   recursivo = %.2f   (teórico: 3)%n",
                    nMinimo, pendienteLogLog(resultados, true, nMinimo), pendienteLogLog(resultados, false, nMinimo));
        }
    }

    /**
     * Pendiente de la recta de mínimos cuadrados de log(T) frente a log(n),
     * usando solo las dimensiones n >= nMinimo. Si T(n) ≈ c·n^k, la pendiente
     * es k.
     *
     * @param iterativo true para el algoritmo iterativo, false para el recursivo
     */
    public static double pendienteLogLog(List<ResultadoExperimento> resultados, boolean iterativo, int nMinimo) {
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        int cuenta = 0;
        for (ResultadoExperimento r : resultados) {
            if (r.dimension < nMinimo) {
                continue;
            }
            double x = Math.log(r.dimension);
            double y = Math.log(iterativo ? r.minIterativo : r.minRecursivo);
            sx += x;
            sy += y;
            sxx += x * x;
            sxy += x * y;
            cuenta++;
        }
        return (cuenta * sxy - sx * sy) / (cuenta * sxx - sx * sx);
    }

    /**
     * Constante c del ajuste por mínimos cuadrados T(n) ≈ c·n³ (en ns),
     * c = Σ T·n³ / Σ n⁶.
     *
     * @param iterativo true para el algoritmo iterativo, false para el recursivo
     */
    public static double constanteAjusteCubico(List<ResultadoExperimento> resultados, boolean iterativo) {
        double numerador = 0, denominador = 0;
        for (ResultadoExperimento r : resultados) {
            double n3 = Math.pow(r.dimension, 3);
            double t = iterativo ? r.minIterativo : r.minRecursivo;
            numerador += t * n3;
            denominador += n3 * n3;
        }
        return numerador / denominador;
    }

    // EXPORTACIÓN DE RESULTADOS

    /**
     * Guarda los resultados en un fichero CSV (separador ";", apto para Excel
     * en español), creando la carpeta de destino si no existe. Los tiempos se
     * guardan en nanosegundos para no perder precisión.
     *
     * @param resultados resultados de los experimentos
     * @param fichero    ruta del CSV de salida (por ejemplo resultados/tiempos.csv)
     * @throws IOException si no se puede escribir el fichero
     */
    public static void guardarCSV(List<ResultadoExperimento> resultados, Path fichero) throws IOException {
        if (fichero.getParent() != null) {
            Files.createDirectories(fichero.getParent());
        }
        try (PrintWriter salida = new PrintWriter(Files.newBufferedWriter(fichero, StandardCharsets.UTF_8))) {
            salida.println("dimension;repeticiones;iterativo_min_ns;iterativo_media_ns;recursivo_min_ns;recursivo_media_ns;rec_entre_iter;resultados_iguales");
            for (ResultadoExperimento r : resultados) {
                // Formato español: coma decimal en el cociente Rec/Iter.
                salida.printf(Locale.forLanguageTag("es-ES"), "%d;%d;%d;%.0f;%d;%.0f;%.2f;%s%n",
                        r.dimension, r.repeticiones,
                        r.minIterativo, r.mediaIterativo,
                        r.minRecursivo, r.mediaRecursivo,
                        cocienteRecursivoIterativo(r),
                        r.resultadosIguales ? "si" : "no");
            }
        }
    }
}
