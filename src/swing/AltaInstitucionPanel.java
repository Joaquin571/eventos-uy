package swing;

import javax.swing.*;
import datatypes.DtInstitucion;
import implementacion.Fabrica;
import interfaces.ISistema;

public class AltaInstitucionPanel {

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    private JPanel mainPanel;
    private JPanel panelFormulario;
    private JPanel panelBotones;
    private JTextField txtNombre;
    private JTextField txtSitioWeb;
    private JTextArea txtDescripcion;
    private JButton btnAceptar;
    private JButton btnCancelar;

    public AltaInstitucionPanel() {

        sistema = Fabrica.getInstance().getISistema();

        configurarEventos();
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
        mostrarExito();
        limpiarFormulario();
        accionCerrar.run();
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