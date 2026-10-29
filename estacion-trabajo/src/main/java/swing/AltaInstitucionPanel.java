package swing;

import javax.swing.*;
import datatypes.DtInstitucion;
import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import java.io.IOException;
import implementacion.Fabrica;
import interfaces.ISistema;

public class AltaInstitucionPanel {

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    private JPanel mainPanel;
    private final ImagenSwing imagen = new ImagenSwing(true);
    private JPanel panelFormulario;
    private JPanel panelBotones;
    private JTextField txtNombre;
    private JTextField txtSitioWeb;
    private JTextArea txtDescripcion;
    private JButton btnAceptar;
    private JButton btnCancelar;

    public AltaInstitucionPanel() {

        sistema = Fabrica.getInstance().getISistema();

        armarUI();
        configurarEventos();
    }


    private void armarUI() {
        txtNombre = new JTextField(24); txtSitioWeb = new JTextField(24);
        txtDescripcion = new JTextArea(3, 24);
        btnAceptar = new JButton("Aceptar"); btnCancelar = new JButton("Cancelar");
        panelFormulario = FormularioSwing.campos("Nombre:", txtNombre, "Descripción:", txtDescripcion, "Sitio web:", txtSitioWeb);
        panelBotones = FormularioSwing.botones(btnAceptar, btnCancelar);
        mainPanel = FormularioSwing.componer(panelBotones, panelFormulario, imagen);
    }

    private void configurarEventos() {

        btnAceptar.addActionListener(
                e -> guardarInstitucion()
        );

        btnCancelar.addActionListener(e -> {
            limpiarFormulario();
            accionCerrar.run();
        });
    }


    private void guardarInstitucion() {
        if (imagen.reintentarAlta(sistema, () -> { limpiarFormulario(); accionCerrar.run(); })) return;
        try {

        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Debe completar nombre y descripción.",
                    "Alta Institución",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (sistema.existeInstitucion(nombre)) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Ya existe una institución con el nombre '" + nombre + "'.",
                    "Alta Institución",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        DtInstitucion dtInstitucion =
                new DtInstitucion(
                        nombre,
                        descripcion,
                        sitioWeb.isEmpty()
                                ? null
                                : sitioWeb
                );

        DtImagen nuevaImagen = imagen.leerValidada(sistema);
        boolean agregada = sistema.altaInstitucion(dtInstitucion);

        if (!agregada) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No se pudo crear la institución.",
                    "Alta Institución",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (!imagen.completarAlta(sistema, TipoEntidadImagen.INSTITUCION, nombre, nuevaImagen)) return;
        mostrarExito();
        limpiarFormulario();
        accionCerrar.run();
        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "Alta Institución", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void mostrarExito() {

        JOptionPane.showMessageDialog(
                mainPanel,
                "Institución creada correctamente.",
                "Alta Institución",
                JOptionPane.INFORMATION_MESSAGE
        );
    }



    private void limpiarFormulario() {
        imagen.mostrar(null);

        txtNombre.setText("");
        txtDescripcion.setText("");
        txtSitioWeb.setText("");
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
}