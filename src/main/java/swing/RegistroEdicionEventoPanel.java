package swing;

import datatypes.DtAsistente;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtRegistro;
import datatypes.DtTipoRegistro;
import datatypes.DtUsuario;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Collection;

public class RegistroEdicionEventoPanel extends JPanel {

    private final ISistema sistema;

    private JComboBox<String> comboAsistentes;
    private JComboBox<String> comboEventos;
    private JComboBox<String> comboEdiciones;
    private JComboBox<String> comboTiposRegistro;
    private JButton btnRegistrar;

    public RegistroEdicionEventoPanel() {
        this.sistema = Fabrica.getInstance().getISistema();

        armarUI();
        configurarEventos();

        refrescarDatos();
    }

    private void armarUI() {

        setLayout(new BorderLayout(10, 10));
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
                                4,
                                2,
                                10,
                                10
                        )
                );

        panelForm.add(new JLabel("Asistente:"));
        comboAsistentes = new JComboBox<>();
        panelForm.add(comboAsistentes);

        panelForm.add(new JLabel("Evento:"));
        comboEventos = new JComboBox<>();
        panelForm.add(comboEventos);

        panelForm.add(new JLabel("Edición del Evento:"));
        comboEdiciones = new JComboBox<>();
        panelForm.add(comboEdiciones);

        panelForm.add(new JLabel("Tipo de Registro:"));
        comboTiposRegistro = new JComboBox<>();
        panelForm.add(comboTiposRegistro);

        btnRegistrar =
                new JButton(
                        "Confirmar Registro"
                );

        JPanel panelBoton =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        panelBoton.add(btnRegistrar);

        add(panelForm, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    private void configurarEventos() {

        comboEventos.addActionListener(
                e -> cargarEdiciones()
        );

        comboEdiciones.addActionListener(
                e -> cargarTiposRegistro()
        );

        btnRegistrar.addActionListener(
                e -> ejecutarRegistro()
        );
    }

    public void refrescarDatos() {

        cargarAsistentes();
        cargarEventos();

        comboEdiciones.removeAllItems();
        comboTiposRegistro.removeAllItems();

        comboAsistentes.setSelectedIndex(-1);
        comboEventos.setSelectedIndex(-1);
        comboEdiciones.setSelectedIndex(-1);
        comboTiposRegistro.setSelectedIndex(-1);
    }

    public void cargarAsistentes() {

        comboAsistentes.removeAllItems();

        Collection<DtUsuario> usuarios =
                sistema.listarUsuarios();

        for (DtUsuario usuario : usuarios) {

            if (usuario instanceof DtAsistente) {

                comboAsistentes.addItem(
                        usuario.getNickname()
                );
            }
        }

        comboAsistentes.setSelectedIndex(-1);
    }

    public void cargarEventos() {

        comboEventos.removeAllItems();

        Collection<DtEvento> eventos =
                sistema.listarEventos();

        for (DtEvento evento : eventos) {

            comboEventos.addItem(
                    evento.getNombre()
            );
        }

        comboEventos.setSelectedIndex(-1);
    }

    private void cargarEdiciones() {

        comboEdiciones.removeAllItems();
        comboTiposRegistro.removeAllItems();

        String eventoSeleccionado =
                (String) comboEventos.getSelectedItem();

        if (eventoSeleccionado == null) {

            comboEdiciones.setSelectedIndex(-1);
            comboTiposRegistro.setSelectedIndex(-1);

            return;
        }

        Collection<DtEdicion> ediciones =
                sistema.obtenerEdicionesEvento(
                        eventoSeleccionado
                );

        for (DtEdicion edicion : ediciones) {

            comboEdiciones.addItem(
                    edicion.getIdNombre()
            );
        }

        comboEdiciones.setSelectedIndex(-1);
        comboTiposRegistro.setSelectedIndex(-1);
    }

    private void cargarTiposRegistro() {

        comboTiposRegistro.removeAllItems();

        String edicionSeleccionada =
                (String) comboEdiciones.getSelectedItem();

        if (edicionSeleccionada == null) {

            comboTiposRegistro.setSelectedIndex(-1);

            return;
        }

        Collection<DtTipoRegistro> tipos =
                sistema.obtenerTiposRegistroEdicion(
                        edicionSeleccionada
                );

        for (DtTipoRegistro tipo : tipos) {

            comboTiposRegistro.addItem(
                    tipo.getIdNombre()
            );
        }

        comboTiposRegistro.setSelectedIndex(-1);
    }

    private void ejecutarRegistro() {

        String nickname =
                (String) comboAsistentes.getSelectedItem();

        String eventoSeleccionado =
                (String) comboEventos.getSelectedItem();

        String edicionSeleccionada =
                (String) comboEdiciones.getSelectedItem();

        String tipoRegistroSeleccionado =
                (String) comboTiposRegistro.getSelectedItem();

        if (nickname == null
                || eventoSeleccionado == null
                || edicionSeleccionada == null
                || tipoRegistroSeleccionado == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar un asistente, un evento, una edición y un tipo de registro.",
                    "Registro a Edición",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            DtTipoRegistro tipoRegistro =
                    sistema.consultarTipoRegistro(
                            edicionSeleccionada,
                            tipoRegistroSeleccionado
                    );

            if (tipoRegistro == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "El tipo de registro seleccionado no existe.",
                        "Registro a Edición",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            DtRegistro dtRegistro =
                    new DtRegistro(
                            LocalDate.now(),
                            tipoRegistro.getCosto(),
                            tipoRegistro.getIdNombre(),
                            edicionSeleccionada
                    );

            sistema.registroAEdicion(
                    nickname,
                    edicionSeleccionada,
                    tipoRegistroSeleccionado,
                    dtRegistro
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Registro realizado correctamente.",
                    "Registro a Edición",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refrescarDatos();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Registro a Edición",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ocurrió un error inesperado:\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
