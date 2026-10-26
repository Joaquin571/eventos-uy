package swing;

import datatypes.DtAsistente;
import datatypes.DtRegistro;
import datatypes.DtUsuario;

import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConsultaRegistroPanel extends JPanel {

    private final ISistema sistema;

    private JComboBox<String> comboAsistentes;
    private JComboBox<DtRegistro> comboRegistros;

    private JTextField txtEdicion;
    private JTextField txtTipoRegistro;
    private JTextField txtFecha;
    private JTextField txtCosto;

    public ConsultaRegistroPanel() {

        this.sistema =
                Fabrica
                        .getInstance()
                        .getISistema();

        armarUI();

        configurarEventos();

        refrescarDatos();
    }

    // =====================================================
    // ARMAR UI
    // =====================================================

    private void armarUI() {

        setLayout(
                new BorderLayout(
                        15,
                        15
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JPanel panelForm =
                new JPanel(
                        new GridLayout(
                                6,
                                2,
                                10,
                                10
                        )
                );

        panelForm.add(
                new JLabel("Asistente:")
        );

        comboAsistentes =
                new JComboBox<>();

        panelForm.add(
                comboAsistentes
        );

        panelForm.add(
                new JLabel("Registros:")
        );

        comboRegistros =
                new JComboBox<>();

        panelForm.add(
                comboRegistros
        );

        panelForm.add(
                new JLabel("Edición:")
        );

        txtEdicion =
                new JTextField();

        txtEdicion.setEditable(
                false
        );

        panelForm.add(
                txtEdicion
        );

        panelForm.add(
                new JLabel(
                        "Tipo de Registro:"
                )
        );

        txtTipoRegistro =
                new JTextField();

        txtTipoRegistro.setEditable(
                false
        );

        panelForm.add(
                txtTipoRegistro
        );

        panelForm.add(
                new JLabel(
                        "Fecha de Registro:"
                )
        );

        txtFecha =
                new JTextField();

        txtFecha.setEditable(
                false
        );

        panelForm.add(
                txtFecha
        );

        panelForm.add(
                new JLabel(
                        "Costo ($):"
                )
        );

        txtCosto =
                new JTextField();

        txtCosto.setEditable(
                false
        );

        panelForm.add(
                txtCosto
        );

        add(
                panelForm,
                BorderLayout.NORTH
        );
    }

    // =====================================================
    // EVENTOS
    // =====================================================

    private void configurarEventos() {

        comboAsistentes.addActionListener(
                e -> cargarRegistros()
        );

        comboRegistros.addActionListener(
                e -> mostrarDetalleRegistro()
        );
    }

    // =====================================================
    // REFRESCAR DATOS
    // =====================================================

    public void refrescarDatos() {

        cargarAsistentes();

        limpiarDetalle();
    }

    // =====================================================
    // CARGAR ASISTENTES
    // =====================================================

    private void cargarAsistentes() {

        comboAsistentes.removeAllItems();

        Collection<DtUsuario> usuarios =
                sistema.listarUsuarios();

        for (DtUsuario usuario :
                usuarios) {

            if (usuario instanceof DtAsistente) {

                comboAsistentes.addItem(
                        usuario.getNickname()
                );
            }
        }

        comboAsistentes.setSelectedIndex(
                -1
        );

        comboRegistros.removeAllItems();

        limpiarDetalle();
    }

    // =====================================================
    // CARGAR REGISTROS DEL ASISTENTE
    // =====================================================

    private void cargarRegistros() {

        comboRegistros.removeAllItems();

        limpiarDetalle();

        String nickname =
                (String)
                        comboAsistentes
                                .getSelectedItem();

        if (nickname == null) {
            return;
        }

        try {

            Collection<DtRegistro> registros =
                    sistema
                            .obtenerRegistrosAsistente(
                                    nickname
                            );

            for (DtRegistro reg :
                    registros) {

                comboRegistros.addItem(
                        reg
                );
            }

            comboRegistros.setSelectedIndex(
                    -1
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al obtener los registros del asistente:\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // MOSTRAR DETALLE
    // =====================================================

    private void mostrarDetalleRegistro() {

        DtRegistro seleccionado =
                (DtRegistro)
                        comboRegistros
                                .getSelectedItem();

        if (seleccionado == null) {

            limpiarDetalle();

            return;
        }

        txtEdicion.setText(
                seleccionado.getNombreEdicion()
                        != null
                        ? seleccionado.getNombreEdicion()
                        : ""
        );

        txtTipoRegistro.setText(
                seleccionado.getNombreTipoRegistro()
                        != null
                        ? seleccionado.getNombreTipoRegistro()
                        : ""
        );

        txtFecha.setText(
                seleccionado.getFechaRegistro()
                        != null
                        ? seleccionado
                        .getFechaRegistro()
                        .toString()
                        : ""
        );

        txtCosto.setText(
                String.valueOf(
                        seleccionado.getCosto()
                )
        );
    }

    // =====================================================
    // NAVEGACIÓN DESDE OTROS CASOS DE USO
    // =====================================================

    public void seleccionarRegistro(
            String nicknameAsistente,
            String nombreEdicion
    ) {

        // Actualizamos asistentes y registros
        refrescarDatos();

        // Seleccionamos el asistente
        comboAsistentes.setSelectedItem(
                nicknameAsistente
        );

        /*
         * Al seleccionar el asistente se dispara
         * automáticamente cargarRegistros().
         *
         * De todas formas, si por algún motivo el listener
         * no se ejecutara, aseguramos que existan los registros.
         */
        if (comboRegistros.getItemCount() == 0) {

            cargarRegistros();
        }

        // Buscamos el registro correspondiente a la edición
        for (
                int i = 0;
                i < comboRegistros.getItemCount();
                i++
        ) {

            DtRegistro registro =
                    comboRegistros.getItemAt(i);

            if (registro != null
                    && registro.getNombreEdicion() != null
                    && registro
                    .getNombreEdicion()
                    .equals(nombreEdicion)) {

                comboRegistros.setSelectedIndex(
                        i
                );

                mostrarDetalleRegistro();

                return;
            }
        }

        // Si no encontramos el registro
        comboRegistros.setSelectedIndex(
                -1
        );

        limpiarDetalle();
    }

    // =====================================================
    // LIMPIAR DETALLE
    // =====================================================

    private void limpiarDetalle() {

        txtEdicion.setText("");

        txtTipoRegistro.setText("");

        txtFecha.setText("");

        txtCosto.setText("");
    }
}