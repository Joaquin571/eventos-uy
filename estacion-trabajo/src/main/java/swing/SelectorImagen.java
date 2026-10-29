package swing;

import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Maneja un botón "Elegir imagen" en los formularios de la Estación de Trabajo.
 * Se reutiliza en las altas de evento, edición e institución.
 *
 * Uso:
 *   selector = new SelectorImagen(panel, boton, sistema);   // al armar el formulario
 *   selector.asociarA(TipoEntidadImagen.EVENTO, nombre);    // después del alta
 *   selector.limpiar();                                     // al limpiar el formulario
 */
public class SelectorImagen {

    private static final String TEXTO_BOTON = "Elegir imagen (opcional)...";

    private final Component padre;
    private final JButton boton;
    private final transient ISistema sistema;
    private transient DtImagen imagen;

    public SelectorImagen(Component padre, JButton boton, ISistema sistema) {
        this.padre = padre;
        this.boton = boton;
        this.sistema = sistema;

        boton.setText(TEXTO_BOTON);
        boton.addActionListener(e -> elegir());
    }

    private void elegir() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Elegir imagen");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg, gif, webp)", "png", "jpg", "jpeg", "gif", "webp"));

        if (selector.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        String tipo = tipoContenido(archivo.getName());

        if (tipo == null) {
            aviso("Elegí una imagen PNG, JPG, GIF o WEBP.");
            return;
        }

        try {
            DtImagen elegida = new DtImagen(tipo, Files.readAllBytes(archivo.toPath()));
            sistema.validarImagen(elegida);
            imagen = elegida;
            boton.setText(archivo.getName());
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
        } catch (IOException e) {
            aviso("No se pudo leer el archivo.");
        }
    }

    /** Asocia la imagen elegida (si hay una) a la entidad recién creada. */
    public void asociarA(TipoEntidadImagen tipo, String clave) {
        if (imagen == null) {
            return;
        }

        try {
            sistema.asociarImagen(tipo, clave, imagen);
        } catch (IllegalArgumentException e) {
            aviso("Se creó correctamente, pero no se pudo guardar la imagen: " + e.getMessage());
        }
    }

    /** Vuelve al estado inicial: sin imagen elegida. */
    public void limpiar() {
        imagen = null;
        boton.setText(TEXTO_BOTON);
    }

    private String tipoContenido(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();

        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return "image/jpeg";
        if (nombre.endsWith(".gif")) return "image/gif";
        if (nombre.endsWith(".webp")) return "image/webp";
        return null;
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Imagen", JOptionPane.WARNING_MESSAGE);
    }
}
