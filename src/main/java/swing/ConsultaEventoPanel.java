package swing;

import datatypes.DtEvento;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConsultaEventoPanel extends JPanel {

    private JComboBox<DtEvento> comboEventos;

    private JTextField txtNombre;
    private JTextField txtSigla;
    private JTextArea txtDescripcion;
    private JTextField txtFechaAlta;
    private JTextArea txtCategorias;

    private final ISistema sistema;

    public ConsultaEventoPanel() {

        sistema = Fabrica.getInstance().getISistema();

        setLayout(new BorderLayout(10, 10));

        // =========================
        // PANEL SUPERIOR
        // =========================

        JPanel panelTop =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelTop.add(
                new JLabel("Seleccionar Evento:")
        );

        comboEventos = new JComboBox<>();

        comboEventos.setPreferredSize(
                new Dimension(250, 25)
        );

        panelTop.add(comboEventos);

        add(
                panelTop,
                BorderLayout.NORTH
        );


        // =========================
        // PANEL CENTRAL
        // =========================

        JPanel panelCenter =
                new JPanel(
                        new GridLayout(5, 2, 5, 5)
                );

        txtNombre = new JTextField();
        txtNombre.setEditable(false);

        txtSigla = new JTextField();
        txtSigla.setEditable(false);

        txtFechaAlta = new JTextField();
        txtFechaAlta.setEditable(false);

        txtDescripcion =
                new JTextArea(3, 20);

        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        txtCategorias =
                new JTextArea(3, 20);

        txtCategorias.setEditable(false);
        txtCategorias.setLineWrap(true);
        txtCategorias.setWrapStyleWord(true);


        panelCenter.add(
                new JLabel("Nombre:")
        );
        panelCenter.add(txtNombre);


        panelCenter.add(
                new JLabel("Sigla:")
        );
        panelCenter.add(txtSigla);


        panelCenter.add(
                new JLabel("Fecha de Alta:")
        );
        panelCenter.add(txtFechaAlta);


        panelCenter.add(
                new JLabel("Descripción:")
        );
        panelCenter.add(
                new JScrollPane(txtDescripcion)
        );


        panelCenter.add(
                new JLabel("Categorías:")
        );
        panelCenter.add(
                new JScrollPane(txtCategorias)
        );


        add(
                panelCenter,
                BorderLayout.CENTER
        );


        // =========================
        // EVENTOS
        // =========================

        comboEventos.addActionListener(
                e -> cargarDatosEvento()
        );

        cargarComboEventos();
    }


    // =========================
    // CARGAR EVENTOS
    // =========================

    public void cargarComboEventos() {

        comboEventos.removeAllItems();

        Collection<DtEvento> eventos =
                sistema.listarEventos();

        if (eventos == null ||
                eventos.isEmpty()) {

            limpiarCampos();
            return;
        }

        for (DtEvento evento : eventos) {
            comboEventos.addItem(evento);
        }

        comboEventos.setSelectedIndex(-1);

        limpiarCampos();
    }


    // =========================
    // MOSTRAR EVENTO
    // =========================

    private void cargarDatosEvento() {

        DtEvento evento =
                (DtEvento) comboEventos.getSelectedItem();

        if (evento == null) {
            limpiarCampos();
            return;
        }

        txtNombre.setText(
                evento.getNombre()
        );

        txtSigla.setText(
                evento.getSigla()
        );

        txtDescripcion.setText(
                evento.getDescripcion()
        );

        txtFechaAlta.setText(
                evento.getFechaAlta() != null
                        ? evento.getFechaAlta().toString()
                        : ""
        );

        if (evento.getCategorias() != null &&
                !evento.getCategorias().isEmpty()) {

            txtCategorias.setText(
                    String.join(
                            ", ",
                            evento.getCategorias()
                    )
            );

        } else {

            txtCategorias.setText(
                    "Sin categorías"
            );
        }
    }


    private void limpiarCampos() {

        txtNombre.setText("");
        txtSigla.setText("");
        txtDescripcion.setText("");
        txtFechaAlta.setText("");
        txtCategorias.setText("");
    }
}