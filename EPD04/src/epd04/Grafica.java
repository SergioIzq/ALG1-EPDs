package epd04;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Genera la gráfica comparativa de los tiempos de ambos algoritmos usando
 * únicamente la biblioteca estándar de Java (Java2D), sin dependencias
 * externas. Al terminar los experimentos, la gráfica se guarda como imagen
 * PNG en la carpeta "resultados" y se muestra además en una ventana.
 *
 * Contenido de la gráfica:
 *  - Eje X: dimensión n de las matrices n x n.
 *  - Eje Y: tiempo mínimo medido en milisegundos.
 *  - Una curva con marcadores para cada algoritmo (todas las instancias).
 *  - En trazo discontinuo, el ajuste teórico c·n³ de cada algoritmo.
 *
 */
public final class Grafica {

    private static final int ANCHO = 1600;
    private static final int ALTO = 1000;
    private static final int MARGEN_IZQ = 140;
    private static final int MARGEN_DER = 60;
    private static final int MARGEN_SUP = 110;
    private static final int MARGEN_INF = 120;

    private static final Color COLOR_ITERATIVO = new Color(0x1F, 0x77, 0xB4);
    private static final Color COLOR_RECURSIVO = new Color(0xD6, 0x27, 0x28);
    private static final Color COLOR_REJILLA = new Color(0xE3, 0xE3, 0xE3);
    private static final Color COLOR_TEXTO = new Color(0x22, 0x22, 0x22);

    /** Zona rectangular de la gráfica donde se dibujan los datos (en píxeles). */
    private static final int AREA_X = MARGEN_IZQ;
    private static final int AREA_Y = MARGEN_SUP;
    private static final int AREA_ANCHO = ANCHO - MARGEN_IZQ - MARGEN_DER;
    private static final int AREA_ALTO = ALTO - MARGEN_SUP - MARGEN_INF;

    /** Clase de utilidad con métodos estáticos: no se instancia. */
    private Grafica() {
    }

    /**
     * Dibuja la gráfica comparativa en una imagen en memoria.
     *
     * @param resultados resultados de los experimentos (ordenados por n)
     * @return imagen con la gráfica
     */
    public static BufferedImage generarGrafica(List<ResultadoExperimento> resultados) {
        BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fondo blanco.
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ANCHO, ALTO);

        // Rango de los ejes: de 0 al máximo redondeado a un valor "bonito".
        int nMax = resultados.get(resultados.size() - 1).dimension;
        double tMax = 0;
        for (ResultadoExperimento r : resultados) {
            tMax = Math.max(tMax, Math.max(r.minIterativo, r.minRecursivo) / 1e6);
        }
        double pasoX = pasoRedondo(nMax / 10.0);
        double maxX = Math.ceil(nMax / pasoX) * pasoX;
        double pasoY = pasoRedondo(tMax / 8.0);
        double maxY = Math.ceil(tMax * 1.05 / pasoY) * pasoY;

        dibujarEjes(g, maxX, maxY, pasoX, pasoY);

        // Curvas de ajuste teórico c·n³ (discontinuas), debajo de las medidas.
        double cIter = Experimentos.constanteAjusteCubico(resultados, true);
        double cRec = Experimentos.constanteAjusteCubico(resultados, false);
        dibujarAjusteCubico(g, maxX, maxY, cIter, COLOR_ITERATIVO);
        dibujarAjusteCubico(g, maxX, maxY, cRec, COLOR_RECURSIVO);

        // Curvas medidas con marcadores en cada instancia.
        dibujarSerie(g, maxX, maxY, resultados, true, COLOR_ITERATIVO);
        dibujarSerie(g, maxX, maxY, resultados, false, COLOR_RECURSIVO);

        // Títulos y leyenda.
        g.setColor(COLOR_TEXTO);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        centrar(g, "Multiplicación de matrices mágicas: iterativo vs. recursivo", ANCHO / 2, 55);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        centrar(g, "Tiempo mínimo de ejecución frente a la dimensión n (matrices n × n)", ANCHO / 2, 88);

        dibujarLeyenda(g, cIter, cRec);

        g.dispose();
        return imagen;
    }

    /**
     * Guarda la imagen de la gráfica como fichero PNG, creando la carpeta de
     * destino si no existe.
     *
     * @param imagen  imagen generada con generarGrafica
     * @param fichero ruta del PNG de salida (por ejemplo resultados/grafica_comparativa.png)
     * @throws IOException si no se puede escribir el fichero
     */
    public static void guardarImagen(BufferedImage imagen, Path fichero) throws IOException {
        if (fichero.getParent() != null) {
            Files.createDirectories(fichero.getParent());
        }
        ImageIO.write(imagen, "png", fichero.toFile());
    }

    /**
     * Muestra la imagen de la gráfica en una ventana, si el entorno lo
     * permite (por ejemplo, al ejecutar desde NetBeans). En un entorno sin
     * pantalla no hace nada.
     *
     * @param imagen imagen generada con generarGrafica
     * @return true si se ha abierto la ventana
     */
    public static boolean mostrarEnVentana(BufferedImage imagen) {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }
        SwingUtilities.invokeLater(() -> {
            // La imagen se dibuja a gran resolución y se escala para que quepa
            // en pantallas pequeñas manteniendo los bordes suaves.
            java.awt.Image escalada = imagen.getScaledInstance(
                    ANCHO * 3 / 4, ALTO * 3 / 4, java.awt.Image.SCALE_SMOOTH);
            JFrame ventana = new JFrame("EPD 4 - Los grimorios de Frieren: gráfica comparativa");
            ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            ventana.add(new JScrollPane(new JLabel(new ImageIcon(escalada))));
            ventana.pack();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
        return true;
    }

    /**
     * Convierte una coordenada X de datos (dimensión n) a píxeles.
     *
     * @param maxX valor máximo del eje X
     */
    private static double px(double valorX, double maxX) {
        return AREA_X + valorX / maxX * AREA_ANCHO;
    }

    /**
     * Convierte una coordenada Y de datos (tiempo en ms) a píxeles.
     *
     * @param maxY valor máximo del eje Y
     */
    private static double py(double valorY, double maxY) {
        return AREA_Y + AREA_ALTO - valorY / maxY * AREA_ALTO;
    }

    /** Dibuja la rejilla, los ejes, las marcas y sus etiquetas. */
    private static void dibujarEjes(Graphics2D g, double maxX, double maxY, double pasoX, double pasoY) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        FontMetrics fm = g.getFontMetrics();

        // Líneas horizontales de la rejilla y etiquetas del eje Y.
        for (double v = 0; v <= maxY + 1e-9; v += pasoY) {
            int yPix = (int) Math.round(py(v, maxY));
            g.setColor(COLOR_REJILLA);
            g.drawLine(AREA_X, yPix, AREA_X + AREA_ANCHO, yPix);
            g.setColor(COLOR_TEXTO);
            String etiqueta = formatear(v, pasoY);
            g.drawString(etiqueta, AREA_X - 12 - fm.stringWidth(etiqueta), yPix + fm.getAscent() / 2 - 2);
        }
        // Líneas verticales de la rejilla y etiquetas del eje X.
        for (double v = 0; v <= maxX + 1e-9; v += pasoX) {
            int xPix = (int) Math.round(px(v, maxX));
            g.setColor(COLOR_REJILLA);
            g.drawLine(xPix, AREA_Y, xPix, AREA_Y + AREA_ALTO);
            g.setColor(COLOR_TEXTO);
            centrar(g, formatear(v, pasoX), xPix, AREA_Y + AREA_ALTO + 32);
        }

        // Ejes.
        g.setColor(COLOR_TEXTO);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(AREA_X, AREA_Y + AREA_ALTO, AREA_X + AREA_ANCHO, AREA_Y + AREA_ALTO);
        g.drawLine(AREA_X, AREA_Y, AREA_X, AREA_Y + AREA_ALTO);

        // Títulos de los ejes (el del eje Y, girado 90 grados).
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        centrar(g, "Dimensión n de las matrices (n × n)", AREA_X + AREA_ANCHO / 2, AREA_Y + AREA_ALTO + 80);
        AffineTransform original = g.getTransform();
        g.rotate(-Math.PI / 2);
        centrar(g, "Tiempo mínimo (ms)", -(AREA_Y + AREA_ALTO / 2), AREA_X - 90);
        g.setTransform(original);
    }

    /** Dibuja la serie medida de un algoritmo: polilínea más un círculo por instancia. */
    private static void dibujarSerie(Graphics2D g, double maxX, double maxY, List<ResultadoExperimento> resultados,
            boolean iterativo, Color color) {
        Path2D linea = new Path2D.Double();
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoExperimento r = resultados.get(i);
            double ms = (iterativo ? r.minIterativo : r.minRecursivo) / 1e6;
            if (i == 0) {
                linea.moveTo(px(r.dimension, maxX), py(ms, maxY));
            } else {
                linea.lineTo(px(r.dimension, maxX), py(ms, maxY));
            }
        }
        g.setColor(color);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(linea);

        for (ResultadoExperimento r : resultados) {
            double ms = (iterativo ? r.minIterativo : r.minRecursivo) / 1e6;
            Ellipse2D punto = new Ellipse2D.Double(px(r.dimension, maxX) - 8, py(ms, maxY) - 8, 16, 16);
            g.setColor(Color.WHITE);
            g.fill(punto);
            g.setColor(color);
            g.setStroke(new BasicStroke(3.5f));
            g.draw(punto);
        }
    }

    /** Dibuja la curva teórica c·n³ en trazo discontinuo. */
    private static void dibujarAjusteCubico(Graphics2D g, double maxX, double maxY, double cNs, Color color) {
        Path2D curva = new Path2D.Double();
        int pasos = 200;
        for (int i = 0; i <= pasos; i++) {
            double n = maxX * i / pasos;
            double ms = cNs * n * n * n / 1e6;
            if (ms > maxY) {
                break; // no se dibuja por encima del área de la gráfica
            }
            if (i == 0) {
                curva.moveTo(px(n, maxX), py(ms, maxY));
            } else {
                curva.lineTo(px(n, maxX), py(ms, maxY));
            }
        }
        Stroke discontinuo = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                10f, new float[]{14f, 10f}, 0f);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.setStroke(discontinuo);
        g.draw(curva);
    }

    /** Dibuja la leyenda en la esquina superior izquierda del área. */
    private static void dibujarLeyenda(Graphics2D g, double cIter, double cRec) {
        String[] textos = {
            "Iterativo (medido)",
            "Recursivo divide y vencerás (medido)",
            String.format(Locale.ROOT, "Ajuste iterativo  %.3f·n³ ns", cIter),
            String.format(Locale.ROOT, "Ajuste recursivo  %.3f·n³ ns", cRec)
        };
        Color[] colores = {COLOR_ITERATIVO, COLOR_RECURSIVO, COLOR_ITERATIVO, COLOR_RECURSIVO};
        boolean[] discontinua = {false, false, true, true};

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        FontMetrics fm = g.getFontMetrics();
        int anchoTexto = 0;
        for (String t : textos) {
            anchoTexto = Math.max(anchoTexto, fm.stringWidth(t));
        }
        int x = AREA_X + 30;
        int y = AREA_Y + 25;
        int altoLinea = 38;
        int anchoCaja = 90 + anchoTexto + 20;
        int altoCaja = altoLinea * textos.length + 20;

        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(x, y, anchoCaja, altoCaja, 14, 14);
        g.setColor(new Color(0xBB, 0xBB, 0xBB));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, anchoCaja, altoCaja, 14, 14);

        for (int i = 0; i < textos.length; i++) {
            int yLinea = y + 10 + altoLinea * i + altoLinea / 2;
            g.setColor(colores[i]);
            if (discontinua[i]) {
                g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                        10f, new float[]{14f, 10f}, 0f));
                g.drawLine(x + 15, yLinea, x + 75, yLinea);
            } else {
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x + 15, yLinea, x + 75, yLinea);
                Ellipse2D punto = new Ellipse2D.Double(x + 45 - 8, yLinea - 8, 16, 16);
                g.setColor(Color.WHITE);
                g.fill(punto);
                g.setColor(colores[i]);
                g.setStroke(new BasicStroke(3.5f));
                g.draw(punto);
            }
            g.setColor(COLOR_TEXTO);
            g.drawString(textos[i], x + 90, yLinea + fm.getAscent() / 2 - 3);
        }
    }

    /** Escribe un texto centrado horizontalmente en x, con la línea base en y. */
    private static void centrar(Graphics2D g, String texto, int x, int y) {
        g.drawString(texto, x - g.getFontMetrics().stringWidth(texto) / 2, y);
    }

    /**
     * Redondea un paso de escala a 1, 2 o 5 por una potencia de 10 (por
     * ejemplo 0.2, 50, 100...) para que las marcas de los ejes sean legibles.
     */
    private static double pasoRedondo(double bruto) {
        double potencia = Math.pow(10, Math.floor(Math.log10(bruto)));
        double fraccion = bruto / potencia;
        double redondo = fraccion <= 1 ? 1 : fraccion <= 2 ? 2 : fraccion <= 5 ? 5 : 10;
        return redondo * potencia;
    }

    /** Formatea una etiqueta del eje con los decimales que exija su paso. */
    private static String formatear(double valor, double paso) {
        int decimales = paso >= 1 ? 0 : (int) Math.ceil(-Math.log10(paso));
        return String.format(Locale.ROOT, "%." + decimales + "f", valor);
    }
}
