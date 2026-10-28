package epd04;

/**
 * Hechizos de combinación de energía: multiplicación de matrices mediante un
 * algoritmo iterativo y mediante un algoritmo recursivo (divide y vencerás).
 *
 * Dadas A (m x n) y B (n x p), el producto C = A · B es una matriz m x p en
 * la que cada elemento es
 *
 *      C[i][j] = sumatorio, para k = 0 .. n-1, de A[i][k] * B[k][j]
 *
 * Ambos algoritmos calculan exactamente los mismos m·n·p productos escalares;
 * solo cambia la forma de organizar el trabajo.
 *
 */
public final class MultiplicacionMatrices {

    /** Clase de utilidad con métodos estáticos: no se instancia. */
    private MultiplicacionMatrices() {
    }

    /**
     * Comprueba si dos matrices se pueden multiplicar: el número de columnas
     * de A debe coincidir con el número de filas de B. Además, ambas deben
     * ser rectangulares (todas sus filas con la misma longitud) y no vacías.
     *
     * @param a primera matriz
     * @param b segunda matriz
     * @return true si el producto A · B está definido
     */
    public static boolean sonCompatibles(int[][] a, int[][] b) {
        return esRectangular(a) && esRectangular(b) && a[0].length == b.length;
    }

    /**
     * Indica si una matriz no es nula, tiene al menos una fila y una columna,
     * y todas sus filas tienen la misma longitud.
     */
    private static boolean esRectangular(int[][] matriz) {
        if (matriz == null || matriz.length == 0 || matriz[0] == null || matriz[0].length == 0) {
            return false;
        }
        int columnas = matriz[0].length;
        for (int[] fila : matriz) {
            if (fila == null || fila.length != columnas) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lanza una excepción descriptiva si A y B no se pueden multiplicar.
     */
    private static void validarCompatibles(int[][] a, int[][] b) {
        if (!sonCompatibles(a, b)) {
            throw new IllegalArgumentException(
                    "Matrices incompatibles para el producto: "
                    + describir(a) + " · " + describir(b)
                    + " (las columnas de A deben coincidir con las filas de B)");
        }
    }

    /** Devuelve las dimensiones de una matriz en formato "filas x columnas". */
    private static String describir(int[][] matriz) {
        if (matriz == null) {
            return "null";
        }
        int columnas = (matriz.length > 0 && matriz[0] != null) ? matriz[0].length : 0;
        return matriz.length + "x" + columnas;
    }

    /**
     * Multiplica A (m x n) por B (n x p) con el algoritmo iterativo clásico de
     * tres bucles anidados.
     *
     * - El bucle i recorre las filas de A (y de C).
     * - El bucle j recorre las columnas de B (y de C).
     * - El bucle k recorre la dimensión común: acumula el producto escalar de
     *   la fila i de A por la columna j de B.
     *
     * Coste: el cuerpo del bucle interno se ejecuta exactamente m·n·p veces,
     * por lo que T(m, n, p) pertenece a Θ(m·n·p); para matrices cuadradas,
     * Θ(n³). No hay mejor ni peor caso: el trabajo no depende de los valores.
     *
     * @param a matriz izquierda (m x n)
     * @param b matriz derecha (n x p)
     * @return matriz producto C (m x p)
     * @throws IllegalArgumentException si las dimensiones no son compatibles
     */
    public static int[][] multiplicarIterativo(int[][] a, int[][] b) {
        validarCompatibles(a, b);

        int m = a.length;      // filas de A = filas de C
        int n = b.length;      // columnas de A = filas de B (dimensión común)
        int p = b[0].length;   // columnas de B = columnas de C

        int[][] c = new int[m][p];

        for (int i = 0; i < m; i++) {
            int[] filaA = a[i]; // referencia a la fila i de A (evita indexar a[i] en cada paso)
            for (int j = 0; j < p; j++) {
                // Producto escalar de la fila i de A por la columna j de B.
                int suma = 0;
                for (int k = 0; k < n; k++) {
                    suma += filaA[k] * b[k][j];
                }
                c[i][j] = suma;
            }
        }
        return c;
    }

    /**
     * Multiplica A (m x n) por B (n x p) con un algoritmo recursivo de divide
     * y vencerás.
     *
     * Idea: el producto es un problema de tres dimensiones (m, n, p). En cada
     * llamada se parte por la mitad la dimensión más grande y se resuelven
     * recursivamente los dos subproblemas resultantes.

     * Los bloques no se copian: cada subproblema se identifica mediante
     * desplazamientos (filaA, comun, colB) y tamaños (m, n, p) sobre las
     * matrices originales, así que "combinar" las soluciones no cuesta nada:
     * cada caso base escribe directamente en su posición de C.
     *
     * Funciona con cualquier dimensión (no exige potencias de 2 ni matrices
     * cuadradas) porque las mitades pueden ser desiguales (⌊x/2⌋ y ⌈x/2⌉).
     *
     * Coste: el árbol de recursión es binario y tiene exactamente m·n·p hojas
     * (una por cada producto escalar), luego tiene 2·m·n·p − 1 llamadas y el
     * coste pertenece a Θ(m·n·p); para matrices cuadradas Θ(n³), lo que
     * coincide con el teorema maestro aplicado a T(n) = 8·T(n/2) + Θ(1).
     *
     * @param a matriz izquierda (m x n)
     * @param b matriz derecha (n x p)
     * @return matriz producto C (m x p)
     * @throws IllegalArgumentException si las dimensiones no son compatibles
     */
    public static int[][] multiplicarRecursivo(int[][] a, int[][] b) {
        validarCompatibles(a, b);

        int m = a.length;
        int n = b.length;
        int p = b[0].length;

        // Java inicializa la matriz a ceros, lo que permite acumular (+=) en
        // los casos base sin un paso previo de inicialización.
        int[][] c = new int[m][p];

        // Llamada inicial: el subproblema es el producto completo.
        multiplicarRecursivo(a, b, c, 0, 0, 0, m, n, p);
        return c;
    }

    /**
     * Paso recursivo: acumula en C el producto del bloque de A que empieza en
     * (filaA, comun) con tamaño m x n por el bloque de B que empieza en
     * (comun, colB) con tamaño n x p. El bloque de C afectado empieza en
     * (filaA, colB) y tiene tamaño m x p.
     *
     * @param a matriz izquierda completa
     * @param b matriz derecha completa
     * @param c matriz resultado completa (se acumula sobre ella)
     * @param filaA primera fila del bloque de A (y de C)
     * @param comun primera columna del bloque de A = primera fila del bloque de B
     * @param colB  primera columna del bloque de B (y de C)
     * @param m número de filas del bloque de A
     * @param n tamaño de la dimensión común del bloque
     * @param p número de columnas del bloque de B
     */
    private static void multiplicarRecursivo(int[][] a, int[][] b, int[][] c,
            int filaA, int comun, int colB,
            int m, int n, int p) {

        // CASO BASE: bloques 1x1. Se calcula un único producto escalar y se
        // acumula en su celda de C (la suma con otros términos de la misma
        // celda la realizan otras hojas del árbol de recursión).
        if (m == 1 && n == 1 && p == 1) {
            c[filaA][colB] += a[filaA][comun] * b[comun][colB];
            return;
        }

        // CASO RECURSIVO: se divide por la mitad la dimensión más grande. Así
        // los subproblemas se mantienen lo más "cuadrados" posible y la
        // profundidad del árbol es ⌈log2 m⌉ + ⌈log2 n⌉ + ⌈log2 p⌉.
        if (m >= n && m >= p) {
            // 1) Partir las filas de A (y de C): C1 = A1·B, C2 = A2·B
            int mitad = m / 2;
            multiplicarRecursivo(a, b, c, filaA, comun, colB, mitad, n, p);
            multiplicarRecursivo(a, b, c, filaA + mitad, comun, colB, m - mitad, n, p);
        } else if (p >= n) {
            // 2) Partir las columnas de B (y de C): C1 = A·B1, C2 = A·B2
            int mitad = p / 2;
            multiplicarRecursivo(a, b, c, filaA, comun, colB, m, n, mitad);
            multiplicarRecursivo(a, b, c, filaA, comun, colB + mitad, m, n, p - mitad);
        } else {
            // 3) Partir la dimensión común: C = A1·B1 + A2·B2 (ambas mitades
            //    se acumulan sobre el mismo bloque de C)
            int mitad = n / 2;
            multiplicarRecursivo(a, b, c, filaA, comun, colB, m, mitad, p);
            multiplicarRecursivo(a, b, c, filaA, comun + mitad, colB, m, n - mitad, p);
        }
    }

    /**
     * Compara dos matrices elemento a elemento.
     *
     * @return true si tienen las mismas dimensiones y los mismos valores
     */
    public static boolean sonIguales(int[][] x, int[][] y) {
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (!java.util.Arrays.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cuenta las llamadas que realiza el algoritmo recursivo para un producto
     * A (m x n) · B (n x p). Reproduce exactamente la misma estrategia de
     * división que multiplicarRecursivo, pero sin
     * operar con matrices, de modo que sirve para comprobar empíricamente la
     * fórmula teórica 2·m·n·p − 1.
     *
     * @return número total de llamadas (la inicial incluida)
     */
    public static long contarLlamadasRecursivas(int m, int n, int p) {
        if (m == 1 && n == 1 && p == 1) {
            return 1; // una hoja: un producto escalar
        }
        if (m >= n && m >= p) {
            return 1 + contarLlamadasRecursivas(m / 2, n, p) + contarLlamadasRecursivas(m - m / 2, n, p);
        } else if (p >= n) {
            return 1 + contarLlamadasRecursivas(m, n, p / 2) + contarLlamadasRecursivas(m, n, p - p / 2);
        } else {
            return 1 + contarLlamadasRecursivas(m, n / 2, p) + contarLlamadasRecursivas(m, n - n / 2, p);
        }
    }
}
