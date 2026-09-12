package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtTipoRegistro;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
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

    private Runnable accionCerrar = () -> {};

    public AltaTipoRegistro() {

        sistema =
                Fabrica.getInstance()
                        .getISistema();

        configurarComponentes();
        configurarEventos();
        refrescarDatos();
    }

    private void configurarComponentes() {

        snrCosto.setModel(
                new SpinnerNumberModel(
                        0.0,
                        0.0,
                        999999999.0,
                        1.0
                )
        );

        snrCosto.setEditor(
                new JSpinner.NumberEditor(
                        snrCosto,
                        "0.00"
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

        snrCupos.setEditor(
                new JSpinner.NumberEditor(
                        snrCupos,
                        "0"
                )
        );

        Dimension comboSize =
                new Dimension(320, 28);

        cbxEvento.setPreferredSize(comboSize);
        cbxEvento.setMinimumSize(
                new Dimension(180, 28)
        );
        cbxEvento.setMaximumSize(
                new Dimension(600, 28)
        );
        cbxEvento.setMaximumRowCount(10);

        cbxEdicion.setPreferredSize(comboSize);
        cbxEdicion.setMinimumSize(
                new Dimension(180, 28)
        );
        cbxEdicion.setMaximumSize(
                new Dimension(600, 28)
        );
        cbxEdicion.setMaximumRowCount(10);

        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
    }

    private void configurarEventos() {

        cbxEvento.addActionListener(
                e -> cargarEdiciones()
        );

        btnAceptar.addActionListener(
                e -> altaTipoRegistro()
        );

        btnCancelar.addActionListener(
                e -> {
                    limpiarFormulario();
                    accionCerrar.run();
                }
        );
    }

    public void refrescarDatos() {

        cargarEventos();

        txtNombre.setText("");
        txtDescripcion.setText("");

        snrCosto.setValue(0.0);
        snrCupos.setValue(1);
    }

    private void cargarEventos() {

        cbxEvento.removeAllItems();
        cbxEdicion.removeAllItems();

        Collection<DtEvento> eventos =
                sistema.listarEventos();

        for (DtEvento evento : eventos) {

            cbxEvento.addItem(
                    evento.getNombre()
            );
        }

        if (cbxEvento.getItemCount() > 0) {

            cbxEvento.setSelectedIndex(0);
            cargarEdiciones();

        } else {

            cbxEvento.setSelectedIndex(-1);
            cbxEdicion.setSelectedIndex(-1);
        }
    }

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

        } else {

            cbxEdicion.setSelectedIndex(-1);
        }
    }

    private void altaTipoRegistro() {

        String nombreEvento =
                (String) cbxEvento.getSelectedItem();

        String nombreEdicion =
                (String) cbxEdicion.getSelectedItem();

        String nombre =
                txtNombre.getText().trim();

        String descripcion =
                txtDescripcion.getText().trim();

        if (nombreEvento == null) {

            mostrarAdvertencia(
                    "Debe seleccionar un evento."
            );

            return;
        }

        if (nombreEdicion == null) {

            mostrarAdvertencia(
                    "Debe seleccionar una edición."
            );

            return;
        }

        if (nombre.isBlank()) {

            mostrarAdvertencia(
                    "Debe ingresar un nombre para el tipo de registro."
            );

            return;
        }

        try {

            snrCosto.commitEdit();
            snrCupos.commitEdit();

        } catch (ParseException e) {

            mostrarAdvertencia(
                    "Costo o cupo tienen un formato inválido."
            );

            return;
        }

        float costo =
                ((Number) snrCosto.getValue())
                        .floatValue();

        int cupo =
                ((Number) snrCupos.getValue())
                        .intValue();

        if (costo < 0) {

            mostrarAdvertencia(
                    "El costo no puede ser negativo."
            );

            return;
        }

        if (cupo <= 0) {

            mostrarAdvertencia(
                    "El cupo debe ser mayor que 0."
            );

            return;
        }

        DtTipoRegistro dtTipoRegistro =
                new DtTipoRegistro(
                        nombre,
                        descripcion,
                        costo,
                        cupo
                );

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

    private void mostrarAdvertencia(
            String mensaje
    ) {

        JOptionPane.showMessageDialog(
                principalPanel,
                mensaje,
                "Alta Tipo de Registro",
                JOptionPane.WARNING_MESSAGE
        );
    }

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

    public void setAccionCerrar(
            Runnable accionCerrar
    ) {

        this.accionCerrar =
                accionCerrar != null
                        ? accionCerrar
                        : () -> {};
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
