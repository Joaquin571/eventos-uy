package swing;

import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;

/** Selección opcional y presentación de imágenes, sin acceder al almacenamiento. */
final class ImagenSwing extends JPanel {
    private final JLabel vista = new JLabel("Sin imagen", SwingConstants.CENTER);
    private final JLabel nombre = new JLabel("Sin selección nueva");
    private Path seleccion;
    private DtImagen actual;
    private TipoEntidadImagen tipoPendiente;
    private String clavePendiente;
    private DtImagen imagenPendiente;

    ImagenSwing(boolean editable) {
        super(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder("Imagen"));
        vista.setPreferredSize(new Dimension(160, 90));
        add(vista, BorderLayout.CENTER);
        if (editable) {
            JButton elegir = new JButton("Seleccionar...");
            JButton quitar = new JButton("Quitar selección");
            JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
            botones.add(elegir); botones.add(quitar);
            JPanel pie = new JPanel(new BorderLayout());
            pie.add(nombre, BorderLayout.NORTH); pie.add(botones, BorderLayout.SOUTH);
            add(pie, BorderLayout.SOUTH);
            elegir.addActionListener(e -> elegir());
            quitar.addActionListener(e -> { seleccion = null; nombre.setText("Sin selección nueva"); presentar(actual); });
        }
    }

    private void elegir() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes PNG, JPEG, GIF y WEBP (hasta 2 MiB)", "png", "jpg", "jpeg", "gif", "webp"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                seleccionar(selector.getSelectedFile().toPath());
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Imagen", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    void seleccionar(Path archivo) throws IOException {
        DtImagen imagen = leer(archivo);
        seleccion = archivo;
        nombre.setText(archivo.getFileName().toString());
        presentar(imagen);
    }

    static DtImagen leer(Path archivo) throws IOException {
        if (!Files.isRegularFile(archivo)) throw new IllegalArgumentException("Seleccione un archivo de imagen.");
        // Lectura acotada también si el archivo crece entre la selección y el guardado.
        byte[] bytes;
        try (InputStream entrada = Files.newInputStream(archivo)) {
            bytes = entrada.readNBytes(2 * 1024 * 1024 + 1);
        }
        if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException("La imagen supera el tamaño máximo de 2 MB.");
        String nombre = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        String tipo = nombre.endsWith(".png") ? "image/png" :
                nombre.endsWith(".jpg") || nombre.endsWith(".jpeg") ? "image/jpeg" :
                nombre.endsWith(".gif") ? "image/gif" : nombre.endsWith(".webp") ? "image/webp" : "";
        return new DtImagen(tipo, bytes);
    }

    DtImagen leerValidada(ISistema sistema) throws IOException {
        if (seleccion == null) return null;
        DtImagen imagen = leer(seleccion);
        sistema.validarImagen(imagen);
        return imagen;
    }

    void cargar(ISistema sistema, TipoEntidadImagen tipo, String clave) {
        mostrar(null);
        if (clave == null) return;
        try {
            mostrar(sistema.obtenerImagen(tipo, clave));
        } catch (RuntimeException ex) {
            vista.setText("No se pudo cargar la imagen");
            vista.setToolTipText(ex.getMessage());
        }
    }

    void mostrar(DtImagen imagen) {
        clavePendiente = null;
        tipoPendiente = null;
        imagenPendiente = null;
        seleccion = null;
        actual = imagen;
        nombre.setText("Sin selección nueva");
        presentar(imagen);
    }

    private void presentar(DtImagen imagen) {
        vista.setIcon(null);
        vista.setToolTipText(null);
        vista.setText("Sin imagen");
        if (imagen == null) return;
        try {
            var original = ImageIO.read(new ByteArrayInputStream(imagen.getContenido()));
            if (original == null) {
                vista.setText("Vista previa no disponible");
                return;
            }
            double escala = Math.min(160.0 / original.getWidth(), 90.0 / original.getHeight());
            vista.setIcon(new ImageIcon(original.getScaledInstance(Math.max(1, (int)(original.getWidth()*escala)), Math.max(1, (int)(original.getHeight()*escala)), Image.SCALE_SMOOTH)));
            vista.setText("");
        } catch (IOException | RuntimeException ex) {
            vista.setText("Vista previa no disponible");
        }
    }

    boolean completarAlta(ISistema sistema, TipoEntidadImagen tipo, String clave, DtImagen imagen) {
        String error = asociar(sistema, tipo, clave, imagen);
        if (error == null) return true;
        tipoPendiente = tipo; clavePendiente = clave; imagenPendiente = imagen;
        JOptionPane.showMessageDialog(this, error + " Pulse nuevamente Aceptar/Confirmar para reintentar sólo la imagen, sin repetir el alta.", "Alta parcial", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    boolean reintentarAlta(ISistema sistema, Runnable finalizar) {
        if (clavePendiente == null) return false;
        if (completarAlta(sistema, tipoPendiente, clavePendiente, imagenPendiente)) {
            JOptionPane.showMessageDialog(this, "Imagen asociada correctamente.", "Imagen", JOptionPane.INFORMATION_MESSAGE);
            mostrar(null);
            finalizar.run();
        }
        return true;
    }

    static String asociar(ISistema sistema, TipoEntidadImagen tipo, String clave, DtImagen imagen) {
        if (imagen != null) {
            try {
                sistema.asociarImagen(tipo, clave, imagen);
            } catch (RuntimeException ex) {
                return "Los datos se guardaron, pero no se pudo asociar la imagen: " + ex.getMessage();
            }
        }
        return null;
    }
}
