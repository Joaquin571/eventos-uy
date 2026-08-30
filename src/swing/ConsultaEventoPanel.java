package swing;

import interfaces.ISistema;
import implementacion.Fabrica;
import datatypes.DtEvento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class ConsultaEventoPanel extends JPanel {

    private JComboBox<String> comboEventos;
    private JTextField txtNombre;
    private JTextField txtSigla;
    private JTextArea txtDescripcion;
    private JTextField txtFechaAlta;
    private JTextArea txtCategorias;

    private ISistema sistema;

    public ConsultaEventoPanel() {
        // Obtener la instancia del sistema desde la Fabrica
        sistema = Fabrica.getInstance().getISistema();

        // Configuración de Layout
        setLayout(new BorderLayout(10, 10));

        // --- PANEL SUPERIOR (Selección de Evento) ---
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTop.add(new JLabel("Seleccionar Evento:"));

        comboEventos = new JComboBox<>();
        panelTop.add(comboEventos);
        add(panelTop, BorderLayout.NORTH);

        // --- PANEL CENTRAL (Detalle del Evento) ---
        JPanel panelCenter = new JPanel(new GridLayout(5, 2, 5, 5));

        txtNombre = new JTextField();
        txtNombre.setEditable(false);

        txtSigla = new JTextField();
        txtSigla.setEditable(false);

        txtFechaAlta = new JTextField();
        txtFechaAlta.setEditable(false);

        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);

        txtCategorias = new JTextArea(3, 20);
        txtCategorias.setEditable(false);
        txtCategorias.setLineWrap(true);

        panelCenter.add(new JLabel("Nombre:"));
        panelCenter.add(txtNombre);

        panelCenter.add(new JLabel("Sigla:"));
        panelCenter.add(txtSigla);

        panelCenter.add(new JLabel("Fecha de Alta:"));
        panelCenter.add(txtFechaAlta);

        panelCenter.add(new JLabel("Descripción:"));
        panelCenter.add(new JScrollPane(txtDescripcion));

        panelCenter.add(new JLabel("Categorías:"));
        panelCenter.add(new JScrollPane(txtCategorias));

        add(panelCenter, BorderLayout.CENTER);

        // --- EVENTOS ---
        // Al seleccionar un elemento del ComboBox, actualizar el formulario
        comboEventos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarDatosEvento();
            }
        });

        // Cargar los eventos al abrir/inicializar la pantalla
        cargarComboEventos();
    }



     // Paso 1: Carga los nombres de los eventos en el ComboBox

    public void cargarComboEventos() {
        comboEventos.removeAllItems();
        Collection<String> eventos = sistema.listarEventos();

        if (eventos != null && !eventos.isEmpty()) {
            for (String nombre : eventos) {
                comboEventos.addItem(nombre);
            }
            comboEventos.setSelectedIndex(0); // Seleccionar el primero por defecto
        } else {
            limpiarCampos();
        }
    }


     // Paso 2: Obtiene la información detallada del evento seleccionado

    private void cargarDatosEvento() {
        String eventoSeleccionado = (String) comboEventos.getSelectedItem();

        if (eventoSeleccionado != null) {
            DtEvento dt = sistema.obtenerInformacionEvento(eventoSeleccionado);
            if (dt != null) {
                txtNombre.setText(dt.getNombre());
                txtSigla.setText(dt.getSigla());
                txtDescripcion.setText(dt.getDescripcion());
                txtFechaAlta.setText(dt.getFechaAlta() != null ? dt.getFechaAlta().toString() : "");

                // Formatear las categorías para mostrarlas
                if (dt.getCategorias() != null && !dt.getCategorias().isEmpty()) {
                    txtCategorias.setText(String.join(", ", dt.getCategorias()));
                } else {
                    txtCategorias.setText("Sin categorías");
                }
            }
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