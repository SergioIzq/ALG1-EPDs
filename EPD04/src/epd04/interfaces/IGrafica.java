package epd04.interfaces;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contrato de la gráfica comparativa de tiempos de ambos algoritmos.
 *
 */
public interface IGrafica {

    /**
     * Dibuja la gráfica comparativa en una imagen en memoria.
     *
     * @param resultados resultados de los experimentos (ordenados por n)
     * @return imagen con la gráfica
     */
    BufferedImage generarGrafica(List<IResultadoExperimento> resultados);

    /**
     * Guarda la imagen de la gráfica como fichero PNG.
     *
     * @param imagen imagen generada con generarGrafica
     * @param fichero ruta del PNG de salida
     * @throws IOException si no se puede escribir el fichero
     */
    void guardarImagen(BufferedImage imagen, Path fichero) throws IOException;

    /**
     * Muestra la imagen de la gráfica en una ventana, si el entorno lo permite.
     *
     * @param imagen imagen generada con generarGrafica
     * @return true si se ha abierto la ventana
     */
    boolean mostrarEnVentana(BufferedImage imagen);
}
