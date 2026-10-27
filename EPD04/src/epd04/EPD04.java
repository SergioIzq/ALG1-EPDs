package epd04;

import epd04.interfaces.IExperimentos;
import epd04.interfaces.IGeneradorMatrices;
import epd04.interfaces.IGrafica;
import epd04.interfaces.IMultiplicacionMatrices;
import epd04.interfaces.IResultadoExperimento;
import java.awt.image.BufferedImage;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * EPD 4 - Proyecto evaluable Bloque I: LOS GRIMORIOS DE FRIEREN.
 *
 * Programa principal. Al pulsar "Run" se ejecuta todo automáticamente, sin
 * ninguna intervención manual. Los resultados se muestran por consola, se
 * guardan en la carpeta "resultados" y la gráfica final se abre en una ventana:
 *
 *  FASE 1. Validación de los hechizos: se comprueba con un ejemplo resuelto a
 *          mano, con matrices rectangulares aleatorias y con dimensiones
 *          incompatibles que ambos algoritmos son correctos.
 *  FASE 2. Experimentación: se calienta la JVM y, para cada dimensión, se
 *          generan aleatoriamente las matrices y se miden los tiempos de
 *          ambos algoritmos.
 *  FASE 3. Resultados: tabla de tiempos ordenada y análisis empírico de la
 *          complejidad.
 *  FASE 4. Ficheros de resultados y gráfica: la tabla de tiempos se guarda en
 *          "resultados/tiempos.csv", la gráfica comparativa en
 *          "resultados/grafica_comparativa.png" y la gráfica se muestra
 *          además en una ventana.
 *
 */
public class EPD04 {

    /** Carpeta donde se guardan la tabla de tiempos (CSV) y la gráfica (PNG). */
    private static final Path CARPETA_RESULTADOS = Path.of("resultados");

    /**
     * Punto de entrada del programa.
     *
     * @param args argumentos de la línea de órdenes (no se usan)
     */
    public static void main(String[] args) {
        long inicioPrograma = System.nanoTime();
        configurarConsolaUTF8();
        imprimirCabecera();

        // Objetos del programa, siempre referenciados a través de su interfaz.
        IGeneradorMatrices generador = new GeneradorMatrices();
        IMultiplicacionMatrices multiplicacion = new MultiplicacionMatrices();
        IExperimentos experimentos = new Experimentos(generador, multiplicacion);
        IGrafica graficador = new Grafica(experimentos);

        // Semilla aleatoria distinta en cada ejecución; se muestra para que
        // cualquier experimento pueda reproducirse fijándola.
        long semilla = System.currentTimeMillis();
        generador.establecerSemilla(semilla);
        System.out.println("Semilla aleatoria de esta ejecución: " + semilla);

        titulo("FASE 1: VALIDACIÓN DE LOS HECHIZOS DE COMBINACIÓN");
        validarEjemploManual(generador, multiplicacion);
        validarMatricesRectangulares(generador, multiplicacion);
        validarIncompatibles(generador, multiplicacion);

        titulo("FASE 2: EXPERIMENTACIÓN (matrices aleatorias y medición de tiempos)");
        System.out.println("Calentando la JVM para que el compilador JIT optimice ambos métodos...");
        experimentos.calentarJVM();
        System.out.println("Midiendo (tiempo mínimo de todas las repeticiones):");
        List<IResultadoExperimento> resultados = experimentos.ejecutarTodos(Experimentos.DIMENSIONES);

        titulo("FASE 3: TABLA DE TIEMPOS Y ANÁLISIS EMPÍRICO");
        System.out.println("Tiempos en milisegundos. Rec/Iter = cuántas veces es más lento el recursivo.");
        experimentos.mostrarTabla(resultados);
        System.out.println();
        System.out.println("Relación con la complejidad teórica O(n³):");
        System.out.println("  - T/n³ casi constante  =>  el tiempo crece proporcionalmente a n³.");
        System.out.println("  - Exponente empírico log(T2/T1)/log(n2/n1) próximo a 3.");
        experimentos.mostrarAnalisisEmpirico(resultados);
        System.out.printf(Locale.ROOT, "   Ajuste por mínimos cuadrados:  T_iter(n) = %.4f·n³ ns,  T_rec(n) = %.4f·n³ ns%n",
                experimentos.constanteAjusteCubico(resultados, true),
                experimentos.constanteAjusteCubico(resultados, false));

        boolean todoCorrecto = resultados.stream().allMatch(IResultadoExperimento::isResultadosIguales);
        System.out.println();
        System.out.println(todoCorrecto
                ? "Todos los experimentos han producido el mismo resultado con ambos algoritmos."
                : "ATENCIÓN: algún experimento ha producido resultados distintos.");
        System.out.printf(Locale.ROOT, "Tiempo total de ejecución del programa: %.1f s%n",
                (System.nanoTime() - inicioPrograma) / 1e9);
        // Se muestra la variable de consumo: así los resultados calculados se
        // usan y la JVM no puede descartar los cálculos que se han medido.
        System.out.println("(variable de consumo: " + experimentos.getConsumo() + ")");

        titulo("FASE 4: GRÁFICA COMPARATIVA Y FICHEROS DE RESULTADOS");
        Path ficheroCSV = CARPETA_RESULTADOS.resolve("tiempos.csv");
        try {
            experimentos.guardarCSV(resultados, ficheroCSV);
            System.out.println("Tabla de tiempos guardada en: " + ficheroCSV.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("No se ha podido guardar la tabla de tiempos: " + e.getMessage());
        }

        BufferedImage grafica = graficador.generarGrafica(resultados);
        Path ficheroGrafica = CARPETA_RESULTADOS.resolve("grafica_comparativa.png");
        try {
            graficador.guardarImagen(grafica, ficheroGrafica);
            System.out.println("Gráfica guardada en:          " + ficheroGrafica.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("No se ha podido guardar la gráfica: " + e.getMessage());
        }
        if (graficador.mostrarEnVentana(grafica)) {
            System.out.println("La gráfica comparativa se muestra en una ventana; ciérrela para terminar el programa.");
        } else {
            System.out.println("No hay entorno gráfico disponible: la gráfica no se puede mostrar.");
        }
    }

    // FASE 1: VALIDACIONES

    /**
     * Multiplica un ejemplo pequeño cuyo resultado se conoce de antemano:
     *
     *   | 1 2 3 |   |  7  8 |   |  58  64 |
     *   | 4 5 6 | · |  9 10 | = | 139 154 |
     *               | 11 12 |
     */
    private static void validarEjemploManual(IGeneradorMatrices generador,
            IMultiplicacionMatrices multiplicacion) {
        int[][] a = {{1, 2, 3}, {4, 5, 6}};
        int[][] b = {{7, 8}, {9, 10}, {11, 12}};
        int[][] esperado = {{58, 64}, {139, 154}};

        int[][] iterativo = multiplicacion.multiplicarIterativo(a, b);
        int[][] recursivo = multiplicacion.multiplicarRecursivo(a, b);

        System.out.println("1.1 Ejemplo resuelto a mano (2x3 · 3x2):");
        imprimirMatriz("A", a);
        imprimirMatriz("B", b);
        imprimirMatriz("Iterativo", iterativo);
        imprimirMatriz("Recursivo", recursivo);
        System.out.println("    Coincide con el resultado esperado: iterativo "
                + siNo(multiplicacion.sonIguales(iterativo, esperado))
                + ", recursivo " + siNo(multiplicacion.sonIguales(recursivo, esperado)));

        // Ejemplo con matrices aleatorias pequeñas para ver el generador en acción.
        int[][] x = generador.generarMatrizAleatoria(3, 4);
        int[][] y = generador.generarMatrizAleatoria(4, 2);
        System.out.println();
        System.out.println("1.2 Matrices aleatorias generadas (3x4 · 4x2):");
        imprimirMatriz("X", x);
        imprimirMatriz("Y", y);
        imprimirMatriz("Iterativo", multiplicacion.multiplicarIterativo(x, y));
        imprimirMatriz("Recursivo", multiplicacion.multiplicarRecursivo(x, y));
    }

    /**
     * Compara ambos algoritmos con matrices aleatorias de dimensiones
     * variadas (también rectangulares y no potencias de 2) y comprueba que el
     * número de llamadas recursivas coincide con la fórmula 2·m·n·p − 1.
     */
    private static void validarMatricesRectangulares(IGeneradorMatrices generador,
            IMultiplicacionMatrices multiplicacion) {
        int[][] casos = {
            {1, 1, 1}, {1, 7, 1}, {5, 1, 3}, {2, 3, 4}, {7, 5, 3},
            {13, 17, 11}, {31, 64, 9}, {64, 33, 20}, {100, 50, 200}, {128, 128, 128}
        };
        System.out.println();
        System.out.println("1.3 Matrices aleatorias de dimensiones variadas (A: m x n, B: n x p):");
        System.out.printf("    %-20s %-13s %-15s %-15s%n", "Dimensiones", "Iter = Rec", "Llamadas rec.", "2·m·n·p - 1");
        for (int[] caso : casos) {
            int m = caso[0], n = caso[1], p = caso[2];
            int[][] a = generador.generarMatrizAleatoria(m, n);
            int[][] b = generador.generarMatrizAleatoria(n, p);
            boolean iguales = multiplicacion.sonIguales(
                    multiplicacion.multiplicarIterativo(a, b),
                    multiplicacion.multiplicarRecursivo(a, b));
            long llamadas = multiplicacion.contarLlamadasRecursivas(m, n, p);
            long formula = 2L * m * n * p - 1;
            System.out.printf("    %-20s %-13s %-15d %-15d %s%n",
                    m + "x" + n + " · " + n + "x" + p, siNo(iguales), llamadas, formula,
                    llamadas == formula ? "" : "(no coincide)");
        }
    }

    /** Comprueba que se detectan las dimensiones incompatibles. */
    private static void validarIncompatibles(IGeneradorMatrices generador,
            IMultiplicacionMatrices multiplicacion) {
        System.out.println();
        System.out.println("1.4 Dimensiones incompatibles (2x3 · 2x3):");
        int[][] a = generador.generarMatrizAleatoria(2, 3);
        try {
            multiplicacion.multiplicarIterativo(a, a);
            System.out.println("    ERROR: no se ha detectado la incompatibilidad");
        } catch (IllegalArgumentException e) {
            System.out.println("    Iterativo -> excepción controlada: " + e.getMessage());
        }
        try {
            multiplicacion.multiplicarRecursivo(a, a);
            System.out.println("    ERROR: no se ha detectado la incompatibilidad");
        } catch (IllegalArgumentException e) {
            System.out.println("    Recursivo -> excepción controlada: " + e.getMessage());
        }
    }

    // SALIDA POR CONSOLA

    /**
     * Configura la salida estándar y la de errores para que escriban en
     * UTF-8, de modo que las tildes, la ñ y símbolos como ³ o · se vean
     * correctamente en la consola de NetBeans.
     *
     * En Windows, Java escribe por defecto con la codificación del sistema
     * (Cp1252), pero la ventana de salida de NetBeans interpreta el texto como
     * UTF-8; sin este ajuste, palabras como "excepción" aparecerían como
     * "excepci�n".
     */
    private static void configurarConsolaUTF8() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }

    private static void imprimirCabecera() {
        System.out.println("=".repeat(80));
        System.out.println("  ALGORÍTMICA I - EPD 4: LOS GRIMORIOS DE FRIEREN");
        System.out.println("  Multiplicación de matrices mágicas: algoritmo iterativo vs. recursivo");
        System.out.println("=".repeat(80));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("-".repeat(80));
        System.out.println(texto);
        System.out.println("-".repeat(80));
    }

    /** Imprime una matriz pequeña con su nombre, alineando las columnas. */
    private static void imprimirMatriz(String nombre, int[][] matriz) {
        System.out.printf("    %s (%dx%d):%n", nombre, matriz.length, matriz[0].length);
        for (int[] fila : matriz) {
            StringBuilder linea = new StringBuilder("      |");
            for (int valor : fila) {
                linea.append(String.format("%5d", valor));
            }
            System.out.println(linea.append(" |"));
        }
    }

    private static String siNo(boolean valor) {
        return valor ? "sí" : "NO";
    }
}
