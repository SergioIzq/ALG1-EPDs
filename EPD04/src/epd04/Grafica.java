package epd04;

import epd04.interfaces.IExperimentos;
import epd04.interfaces.IGrafica;
import epd04.interfaces.IResultadoExperimento;

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
public final class Grafica implements IGrafica {

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

    /** Sistema de experimentos que proporciona el ajuste teórico c·n³. */
    private final IExperimentos experimentos;

    /**
     * Crea la gráfica.
     *
     * @param experimentos sistema de experimentos usado para el ajuste c·n³
     */
    public Grafica(IExperimentos experimentos) {
        this.experimentos = experimentos;
    }

    /**
     * Dibuja la gráfica comparativa en una imagen en memoria.
     *
     * @param resultados resultados de los experimentos (ordenados por n)
     * @return imagen con la gráfica
     */
    @Override
    public BufferedImage generarGrafica(List<IResultadoExperimento> resultados) {
        BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fondo blanco.
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ANCHO, ALTO);

        // Rango de los ejes: de 0 al máximo redondeado a un valor "bonito".
        int nMax = resultados.get(resultados.size() - 1).getDimension();
        double tMax = 0;
        for (IResultadoExperimento r : resultados) {
            tMax = Math.max(tMax, Math.max(r.getMinIterativo(), r.getMinRecursivo()) / 1e6);
        }
        double pasoX = pasoRedondo(nMax / 10.0);
        double maxX = Math.ceil(nMax / pasoX) * pasoX;
        double pasoY = pasoRedondo(tMax / 8.0);
        double maxY = Math.ceil(tMax * 1.05 / pasoY) * pasoY;

        Area area = new Area(MARGEN_IZQ, MARGEN_SUP, ANCHO - MARGEN_IZQ - MARGEN_DER,
                ALTO - MARGEN_SUP - MARGEN_INF, maxX, maxY);

        dibujarEjes(g, area, pasoX, pasoY);

        // Curvas de ajuste teórico c·n³ (discontinuas), debajo de las medidas.
        double cIter = experimentos.constanteAjusteCubico(resultados, true);
        double cRec = experimentos.constanteAjusteCubico(resultados, false);
        dibujarAjusteCubico(g, area, cIter, COLOR_ITERATIVO);
        dibujarAjusteCubico(g, area, cRec, COLOR_RECURSIVO);

        // Curvas medidas con marcadores en cada instancia.
        dibujarSerie(g, area, resultados, true, COLOR_ITERATIVO);
        dibujarSerie(g, area, resultados, false, COLOR_RECURSIVO);

        // Títulos y leyenda.
        g.setColor(COLOR_TEXTO);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        centrar(g, "Multiplicación de matrices mágicas: iterativo vs. recursivo", ANCHO / 2, 55);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        centrar(g, "Tiempo mínimo de ejecución frente a la dimensión n (matrices n × n)", ANCHO / 2, 88);

        dibujarLeyenda(g, area, cIter, cRec);

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
    @Override
    public void guardarImagen(BufferedImage imagen, Path fichero) throws IOException {
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
    @Override
    public boolean mostrarEnVentana(BufferedImage imagen) {
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
     * Zona rectangular de la gráfica y conversión de coordenadas de datos
     * (n, ms) a píxeles.
     */
    private static final class Area {

        private final int x;
        private final int y;
        private final int ancho;
        private final int alto;
        private final double maxX;
        private final double maxY;

        Area(int x, int y, int ancho, int alto, double maxX, double maxY) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        int getAncho() {
            return ancho;
        }

        int getAlto() {
            return alto;
        }

        double getMaxX() {
            return maxX;
        }

        double getMaxY() {
            return maxY;
        }

        double px(double valorX) {
            return x + valorX / maxX * ancho;
        }

        double py(double valorY) {
            return y + alto - valorY / maxY * alto;
        }
    }

    /** Dibuja la rejilla, los ejes, las marcas y sus etiquetas. */
    private static void dibujarEjes(Graphics2D g, Area a, double pasoX, double pasoY) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        FontMetrics fm = g.getFontMetrics();

        // Líneas horizontales de la rejilla y etiquetas del eje Y.
        for (double v = 0; v <= a.getMaxY() + 1e-9; v += pasoY) {
            int yPix = (int) Math.round(a.py(v));
            g.setColor(COLOR_REJILLA);
            g.drawLine(a.getX(), yPix, a.getX() + a.getAncho(), yPix);
            g.setColor(COLOR_TEXTO);
            String etiqueta = formatear(v, pasoY);
            g.drawString(etiqueta, a.getX() - 12 - fm.stringWidth(etiqueta), yPix + fm.getAscent() / 2 - 2);
        }
        // Líneas verticales de la rejilla y etiquetas del eje X.
        for (double v = 0; v <= a.getMaxX() + 1e-9; v += pasoX) {
            int xPix = (int) Math.round(a.px(v));
            g.setColor(COLOR_REJILLA);
            g.drawLine(xPix, a.getY(), xPix, a.getY() + a.getAlto());
            g.setColor(COLOR_TEXTO);
            centrar(g, formatear(v, pasoX), xPix, a.getY() + a.getAlto() + 32);
        }

        // Ejes.
        g.setColor(COLOR_TEXTO);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(a.getX(), a.getY() + a.getAlto(), a.getX() + a.getAncho(), a.getY() + a.getAlto());
        g.drawLine(a.getX(), a.getY(), a.getX(), a.getY() + a.getAlto());

        // Títulos de los ejes (el del eje Y, girado 90 grados).
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        centrar(g, "Dimensión n de las matrices (n × n)", a.getX() + a.getAncho() / 2, a.getY() + a.getAlto() + 80);
        AffineTransform original = g.getTransform();
        g.rotate(-Math.PI / 2);
        centrar(g, "Tiempo mínimo (ms)", -(a.getY() + a.getAlto() / 2), a.getX() - 90);
        g.setTransform(original);
    }

    /** Dibuja la serie medida de un algoritmo: polilínea más un círculo por instancia. */
    private static void dibujarSerie(Graphics2D g, Area a, List<IResultadoExperimento> resultados,
            boolean iterativo, Color color) {
        Path2D linea = new Path2D.Double();
        for (int i = 0; i < resultados.size(); i++) {
            IResultadoExperimento r = resultados.get(i);
            double ms = (iterativo ? r.getMinIterativo() : r.getMinRecursivo()) / 1e6;
            if (i == 0) {
                linea.moveTo(a.px(r.getDimension()), a.py(ms));
            } else {
                linea.lineTo(a.px(r.getDimension()), a.py(ms));
            }
        }
        g.setColor(color);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(linea);

        for (IResultadoExperimento r : resultados) {
            double ms = (iterativo ? r.getMinIterativo() : r.getMinRecursivo()) / 1e6;
            Ellipse2D punto = new Ellipse2D.Double(a.px(r.getDimension()) - 8, a.py(ms) - 8, 16, 16);
            g.setColor(Color.WHITE);
            g.fill(punto);
            g.setColor(color);
            g.setStroke(new BasicStroke(3.5f));
            g.draw(punto);
        }
    }

    /** Dibuja la curva teórica c·n³ en trazo discontinuo. */
    private static void dibujarAjusteCubico(Graphics2D g, Area a, double cNs, Color color) {
        Path2D curva = new Path2D.Double();
        int pasos = 200;
        for (int i = 0; i <= pasos; i++) {
            double n = a.getMaxX() * i / pasos;
            double ms = cNs * n * n * n / 1e6;
            if (ms > a.getMaxY()) {
                break; // no se dibuja por encima del área de la gráfica
            }
            if (i == 0) {
                curva.moveTo(a.px(n), a.py(ms));
            } else {
                curva.lineTo(a.px(n), a.py(ms));
            }
        }
        Stroke discontinuo = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                10f, new float[]{14f, 10f}, 0f);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.setStroke(discontinuo);
        g.draw(curva);
    }

    /** Dibuja la leyenda en la esquina superior izquierda del área. */
    private static void dibujarLeyenda(Graphics2D g, Area a, double cIter, double cRec) {
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
        int x = a.getX() + 30;
        int y = a.getY() + 25;
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
