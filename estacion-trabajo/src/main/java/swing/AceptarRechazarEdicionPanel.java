package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.EstadoEdicion;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caso de uso Aceptar/Rechazar Edición de Evento (administrador).
 * Lista los eventos, luego las ediciones en estado Ingresada del evento elegido,
 * y permite aceptar o rechazar la seleccionada.
 */
public class AceptarRechazarEdicionPanel {

    private static final String SELECCIONE_EVENTO = "Seleccione un evento";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient ISistema sistema;

    private JPanel mainPanel;
    private JComboBox<String> comboEvento;
    private DefaultListModel<String> modeloEdiciones;
    private JList<String> listaEdiciones;
    private JTextArea txtDetalle;
    private JButton btnAceptar;
    private JButton btnRechazar;
    private JButton btnCerrar;

    // Ediciones Ingresada del evento elegido, por nombre
    private final Map<String, DtEdicion> ediciones = new LinkedHashMap<>();

    private boolean cargando = false;
    private Runnable accionCerrar = () -> {};

    public AceptarRechazarEdicionPanel() {
        sistema = Fabrica.getInstance().getISistema();

        inicializarComponentes();
        cargarEventos();

        comboEvento.addActionListener(e -> {
            if (!cargando) {
                cargarEdiciones();
            }
        });

        listaEdiciones.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        btnAceptar.addActionListener(e -> cambiarEstado(true));
        btnRechazar.addActionListener(e -> cambiarEstado(false));
        btnCerrar.addActionListener(e -> accionCerrar.run());
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void refrescarDatos() {
        cargarEventos();
    }

    public void cargarEventos() {
        cargando = true;

        comboEvento.removeAllItems();
        comboEvento.addItem(SELECCIONE_EVENTO);

        for (DtEvento evento : sistema.listarEventos()) {
            comboEvento.addItem(evento.getNombre());
        }

        comboEvento.setSelectedIndex(0);
        cargando = false;

        cargarEdiciones();
    }

    private String eventoElegido() {
        Object seleccionado = comboEvento.getSelectedItem();

        if (seleccionado == null || SELECCIONE_EVENTO.equals(seleccionado)) {
            return null;
        }
        return seleccionado.toString();
    }

    private void cargarEdiciones() {
        ediciones.clear();
        modeloEdiciones.clear();

        String evento = eventoElegido();

        if (evento != null) {
            for (DtEdicion edicion : sistema.obtenerEdicionesEvento(evento, EstadoEdicion.INGRESADA)) {
                ediciones.put(edicion.getIdNombre(), edicion);
                modeloEdiciones.addElement(edicion.getIdNombre());
            }
        }

        if (evento == null) {
            txtDetalle.setText("Elija un evento para ver sus ediciones en estado Ingresada.");
        } else if (ediciones.isEmpty()) {
            txtDetalle.setText("El evento no tiene ediciones en estado Ingresada.");
        } else {
            txtDetalle.setText("Elija una edición para ver sus datos.");
        }

        actualizarBotones();
    }

    private void mostrarDetalle() {
        String nombre = listaEdiciones.getSelectedValue();
        DtEdicion edicion = nombre == null ? null : ediciones.get(nombre);

        if (edicion != null) {
            txtDetalle.setText(
                    "Edición: " + edicion.getIdNombre() + "\n"
                            + "Sigla: " + edicion.getSigla() + "\n"
                            + "Ciudad: " + edicion.getCiudad() + "\n"
                            + "País: " + edicion.getPais() + "\n"
                            + "Inicio: " + edicion.getFechaInicio().format(FECHA) + "\n"
                            + "Fin: " + edicion.getFechaFin().format(FECHA) + "\n"
                            + "Fecha de alta: " + edicion.getFechaAlta().format(FECHA) + "\n"
                            + "Organizador: " + edicion.getNombreOrganizador() + "\n"
                            + "Estado: " + edicion.getEstado());
        }

        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean haySeleccion = listaEdiciones.getSelectedValue() != null;
        btnAceptar.setEnabled(haySeleccion);
        btnRechazar.setEnabled(haySeleccion);
    }

    private void cambiarEstado(boolean aceptar) {
        String nombre = listaEdiciones.getSelectedValue();

        if (nombre == null) {
            return;
        }

        String accion = aceptar ? "aceptar" : "rechazar";

        int respuesta = JOptionPane.showConfirmDialog(mainPanel,
                "¿Desea " + accion + " la edición '" + nombre + "'?\n"
                        + "Esta decisión no se puede deshacer.",
                "Aceptar/Rechazar Edición", JOptionPane.YES_NO_OPTION);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            sistema.aceptarRechazarEdicion(nombre, aceptar);

            JOptionPane.showMessageDialog(mainPanel,
                    "La edición '" + nombre + "' fue " + (aceptar ? "aceptada." : "rechazada."),
                    "Aceptar/Rechazar Edición", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(),
                    "Aceptar/Rechazar Edición", JOptionPane.ERROR_MESSAGE);
        }

        // Se vuelve a leer: la edición ya no está Ingresada
        cargarEdiciones();
    }

    private void inicializarComponentes() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Evento
        JPanel panelEvento = new JPanel(new BorderLayout(5, 5));
        panelEvento.add(new JLabel("Evento:"), BorderLayout.WEST);
        comboEvento = new JComboBox<>();
        panelEvento.add(comboEvento, BorderLayout.CENTER);

        // Ediciones Ingresada
        modeloEdiciones = new DefaultListModel<>();
        listaEdiciones = new JList<>(modeloEdiciones);
        listaEdiciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollLista = new JScrollPane(listaEdiciones);
        scrollLista.setBorder(BorderFactory.createTitledBorder("Ediciones en estado Ingresada"));
        scrollLista.setPreferredSize(new Dimension(240, 200));

        // Detalle
        txtDetalle = new JTextArea();
        txtDetalle.setEditable(false);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);

        JScrollPane scrollDetalle = new JScrollPane(txtDetalle);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Datos de la edición"));

        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 10));
        centro.add(scrollLista);
        centro.add(scrollDetalle);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrar = new JButton("Cerrar");
        btnRechazar = new JButton("Rechazar edición");
        btnAceptar = new JButton("Aceptar edición");
        panelBotones.add(btnCerrar);
        panelBotones.add(btnRechazar);
        panelBotones.add(btnAceptar);

        mainPanel.add(panelEvento, BorderLayout.NORTH);
        mainPanel.add(centro, BorderLayout.CENTER);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);
    }
}