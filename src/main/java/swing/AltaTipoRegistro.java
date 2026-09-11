package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtTipoRegistro;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Collection;

public class AltaTipoRegistro {

    private final transient ISistema sistema;

    private JPanel mainPanel;
    private JPanel principalPanel;

    private JComboBox<String> cbxEvento;
    private JTextArea txtDescripcion;
    private JSpinner snrCosto;
    private JSpinner snrCupos;
    private JPanel botonesPanel;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private JComboBox<String> cbxEdicion;
    private JTextField txtNombre;

    public AltaTipoRegistro() {

        sistema =
                Fabrica.getInstance()
                        .getISistema();

        configurarSpinners();
        configurarEventos();
        cargarEventos();
    }

    // =====================================================
    // SPINNERS
    // =====================================================

    private void configurarSpinners() {

        snrCosto.setModel(
                new SpinnerNumberModel(
                        0.0,
                        0.0,
                        Double.MAX_VALUE,
                        1.0
                )
        );

        snrCupos.setModel(
                new SpinnerNumberModel(
                        1,
                        1,
                        Integer.MAX_VALUE,
                        1
                )
        );
    }

    // =====================================================
    // EVENTOS DE LA INTERFAZ
    // =====================================================

    private void configurarEventos() {

        cbxEvento.addItemListener(e -> {

            if (e.getStateChange()
                    == ItemEvent.SELECTED) {

                cargarEdiciones();
            }
        });

        btnAceptar.addActionListener(
                (ActionEvent e) ->
                        altaTipoRegistro()
        );

        btnCancelar.addActionListener(
                e -> limpiarFormulario()
        );
    }

    // =====================================================
    // CARGAR EVENTOS
    // =====================================================

    private void cargarEventos() {

        cbxEvento.removeAllItems();

        Collection<DtEvento> eventos =
                sistema.listarEventos();

        for (DtEvento evento : eventos) {

            cbxEvento.addItem(
                    evento.getNombre()
            );
        }

        // Si no hay eventos no intentamos buscar ediciones
        if (cbxEvento.getItemCount() > 0) {

            cbxEvento.setSelectedIndex(0);
            cargarEdiciones();

        } else {

            cbxEdicion.removeAllItems();
        }
    }

    // =====================================================
    // CARGAR EDICIONES
    // =====================================================

    private void cargarEdiciones() {

        cbxEdicion.removeAllItems();

        String nombreEvento =
                (String) cbxEvento.getSelectedItem();

        if (nombreEvento == null
                || nombreEvento.isBlank()) {

            return;
        }

        Collection<DtEdicion> ediciones =
                sistema.obtenerEdicionesEvento(
                        nombreEvento
                );

        for (DtEdicion edicion : ediciones) {

            cbxEdicion.addItem(
                    edicion.getIdNombre()
            );
        }

        if (cbxEdicion.getItemCount() > 0) {
            cbxEdicion.setSelectedIndex(0);
        }
    }

    // =====================================================
    // ALTA TIPO REGISTRO
    // =====================================================

    private void altaTipoRegistro() {

        String nombreEvento =
                (String) cbxEvento.getSelectedItem();

        String nombreEdicion =
                (String) cbxEdicion.getSelectedItem();

        String nombre =
                txtNombre.getText().trim();

        String descripcion =
                txtDescripcion.getText().trim();

        // -------------------------------------------------
        // VALIDACIONES DE INTERFAZ
        // -------------------------------------------------

        if (nombreEvento == null) {

            JOptionPane.showMessageDialog(
                    principalPanel,
                    "Debe seleccionar un evento.",
                    "Alta Tipo de Registro",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (nombreEdicion == null) {

            JOptionPane.showMessageDialog(
                    principalPanel,
                    "Debe seleccionar una edición.",
                    "Alta Tipo de Registro",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (nombre.isBlank()) {

            JOptionPane.showMessageDialog(
                    principalPanel,
                    "Debe ingresar un nombre para el tipo de registro.",
                    "Alta Tipo de Registro",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // -------------------------------------------------
        // OBTENER COSTO Y CUPO
        // -------------------------------------------------

        float costo =
                ((Number) snrCosto.getValue())
                        .floatValue();

        int cupo =
                ((Number) snrCupos.getValue())
                        .intValue();

        // -------------------------------------------------
        // CREAR DTO
        // -------------------------------------------------

        DtTipoRegistro dtTipoRegistro =
                new DtTipoRegistro(
                        nombre,
                        descripcion,
                        costo,
                        cupo
                );

        // -------------------------------------------------
        // LLAMAR AL SISTEMA
        // -------------------------------------------------

        try {

            boolean ok =
                    sistema.altaTipoRegistro(
                            dtTipoRegistro,
                            nombreEdicion
                    );

            if (ok) {

                JOptionPane.showMessageDialog(
                        principalPanel,
                        "Tipo de registro creado correctamente.",
                        "Alta Tipo de Registro",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

            } else {

                JOptionPane.showMessageDialog(
                        principalPanel,
                        "Ya existe un tipo de registro con ese nombre o no fue posible crearlo.",
                        "Alta Tipo de Registro",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    principalPanel,
                    "Error: " + e.getMessage(),
                    "Alta Tipo de Registro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // LIMPIAR
    // =====================================================

    private void limpiarFormulario() {

        txtNombre.setText("");
        txtDescripcion.setText("");

        snrCosto.setValue(0.0);
        snrCupos.setValue(1);

        if (cbxEvento.getItemCount() > 0) {
            cbxEvento.setSelectedIndex(0);
        }

        cargarEdiciones();
    }

    public void refrescarDatos() {
        cargarEventos();

        txtNombre.setText("");
        txtDescripcion.setText("");

        snrCosto.setValue(0.0);
        snrCupos.setValue(1);
    }

    // =====================================================
    // PANEL PARA PRINCIPAL
    // =====================================================

    public JPanel getMainPanel() {
        return mainPanel;
    }
}