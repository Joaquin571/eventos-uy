package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Collection;

public class ConsultaEventoPanel extends JPanel {

    private JComboBox<DtEvento> comboEventos;

    private JTextField txtNombre;
    private JTextField txtSigla;
    private JTextArea txtDescripcion;
    private JTextField txtFechaAlta;
    private JTextArea txtCategorias;

    private JList<DtEdicion> listEdiciones;
    private DefaultListModel<DtEdicion> listModelEdiciones;

    private final ISistema sistema;

    public ConsultaEventoPanel(principal ventanaPrincipal) {

        sistema = Fabrica.getInstance().getISistema();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Margen alrededor del panel

        // =========================
        // PANEL SUPERIOR (Selección)
        // =========================
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel lblSeleccionar = new JLabel("Seleccionar Evento:");
        lblSeleccionar.setFont(lblSeleccionar.getFont().deriveFont(Font.BOLD));
        panelTop.add(lblSeleccionar);

        comboEventos = new JComboBox<>();
        comboEventos.setPreferredSize(new Dimension(300, 26));
        panelTop.add(comboEventos);

        add(panelTop, BorderLayout.NORTH);

        // =========================
        // PANEL CENTRAL (Formulario)
        // =========================
        JPanel panelCenter = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtNombre = new JTextField();
        txtNombre.setEditable(false);

        txtSigla = new JTextField();
        txtSigla.setEditable(false);

        txtFechaAlta = new JTextField();
        txtFechaAlta.setEditable(false);

        txtDescripcion = new JTextArea(3, 25);
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        txtCategorias = new JTextArea(2, 25);
        txtCategorias.setEditable(false);
        txtCategorias.setLineWrap(true);
        txtCategorias.setWrapStyleWord(true);

        listModelEdiciones = new DefaultListModel<>();
        listEdiciones = new JList<>(listModelEdiciones);
        JScrollPane scrollEdiciones = new JScrollPane(listEdiciones);
        scrollEdiciones.setPreferredSize(new Dimension(0, 100));

        // --- Fila 0: Nombre ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        panelCenter.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelCenter.add(txtNombre, gbc);

        // --- Fila 1: Sigla ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelCenter.add(new JLabel("Sigla:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelCenter.add(txtSigla, gbc);

        // --- Fila 2: Fecha de Alta ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panelCenter.add(new JLabel("Fecha de Alta:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelCenter.add(txtFechaAlta, gbc);

        // --- Fila 3: Descripción ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelCenter.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelCenter.add(new JScrollPane(txtDescripcion), gbc);

        // --- Fila 4: Categorías ---
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panelCenter.add(new JLabel("Categorías:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelCenter.add(new JScrollPane(txtCategorias), gbc);

        // --- Fila 5: Ediciones ---
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        panelCenter.add(new JLabel("Ediciones del Evento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelCenter.add(scrollEdiciones, gbc);

        add(panelCenter, BorderLayout.CENTER);

        // =========================
        // EVENTOS
        // =========================
        comboEventos.addActionListener(e -> cargarDatosEvento());

        // Listener para detectar el doble clic en la lista de ediciones
        listEdiciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DtEdicion edicionSeleccionada = listEdiciones.getSelectedValue();
                    DtEvento eventoSeleccionado = (DtEvento) comboEventos.getSelectedItem();

                    if (edicionSeleccionada != null && eventoSeleccionado != null && ventanaPrincipal != null) {
                        ventanaPrincipal.navegarAConsultaEdicion(
                                eventoSeleccionado.getNombre(),
                                edicionSeleccionada.getIdNombre()
                        );
                    }
                }
            }
        });

        cargarComboEventos();
    }

    // =========================
    // CARGAR EVENTOS
    // =========================
    public void cargarComboEventos() {
        comboEventos.removeAllItems();

        Collection<DtEvento> eventos = sistema.listarEventos();

        if (eventos == null || eventos.isEmpty()) {
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
    // MOSTRAR EVENTO Y SUS EDICIONES
    // =========================
    private void cargarDatosEvento() {
        DtEvento evento = (DtEvento) comboEventos.getSelectedItem();

        if (evento == null) {
            limpiarCampos();
            return;
        }

        txtNombre.setText(evento.getNombre());
        txtSigla.setText(evento.getSigla());
        txtDescripcion.setText(evento.getDescripcion());
        txtFechaAlta.setText(
                evento.getFechaAlta() != null
                        ? evento.getFechaAlta().toString()
                        : ""
        );

        if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
            txtCategorias.setText(String.join(", ", evento.getCategorias()));
        } else {
            txtCategorias.setText("Sin categorías");
        }

        // --- Carga de las Ediciones del Evento ---
        listModelEdiciones.clear();
        Collection<DtEdicion> ediciones = sistema.obtenerEdicionesEvento(evento.getNombre());

        if (ediciones != null && !ediciones.isEmpty()) {
            for (DtEdicion edicion : ediciones) {
                listModelEdiciones.addElement(edicion);
            }
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtSigla.setText("");
        txtDescripcion.setText("");
        txtFechaAlta.setText("");
        txtCategorias.setText("");
        if (listModelEdiciones != null) {
            listModelEdiciones.clear();
        }
    }
}