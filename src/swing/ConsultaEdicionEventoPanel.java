package swing;

import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConsultaEdicionEventoPanel extends JPanel {

    private final ISistema sistema;
    private JComboBox<String> comboEventos;
    private JComboBox<String> comboEdiciones;
    private JTextArea areaDetalles;

    public ConsultaEdicionEventoPanel() {
        this.sistema = Fabrica.getInstance().getISistema();
        initUI();
        cargarEventos();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelFiltros = new JPanel(new GridLayout(2, 2, 10, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Selección de Evento y Edición"));

        panelFiltros.add(new JLabel("Evento:"));
        comboEventos = new JComboBox<>();
        comboEventos.addActionListener(e -> cargarEdiciones());
        panelFiltros.add(comboEventos);

        panelFiltros.add(new JLabel("Edición:"));
        comboEdiciones = new JComboBox<>();
        comboEdiciones.addActionListener(e -> mostrarDetalles());
        panelFiltros.add(comboEdiciones);

        areaDetalles = new JTextArea();
        areaDetalles.setEditable(false);
        areaDetalles.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(areaDetalles);
        scroll.setBorder(BorderFactory.createTitledBorder("Información Detallada de la Edición"));

        add(panelFiltros, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void cargarEventos() {
        comboEventos.removeAllItems();
        Collection<DtEvento> eventos = sistema.listarEventos();
        for (DtEvento ev : eventos) {
            comboEventos.addItem(ev.getNombre());
        }
    }

    private void cargarEdiciones() {
        comboEdiciones.removeAllItems();
        String eventoSel = (String) comboEventos.getSelectedItem();
        if (eventoSel != null) {
            Collection<DtEdicion> ediciones = sistema.obtenerEdicionesEvento(eventoSel);
            for (DtEdicion ed : ediciones) {
                comboEdiciones.addItem(ed.getIdNombre());
            }
        }
    }

    private void mostrarDetalles() {
        String edicionSel = (String) comboEdiciones.getSelectedItem();
        if (edicionSel == null) {
            areaDetalles.setText("");
            return;
        }

        String eventoSel = (String) comboEventos.getSelectedItem();
        Collection<DtEdicion> ediciones = sistema.obtenerEdicionesEvento(eventoSel);
        DtEdicion seleccionada = null;

        for (DtEdicion ed : ediciones) {
            if (ed.getIdNombre().equalsIgnoreCase(edicionSel)) {
                seleccionada = ed;
                break;
            }
        }

        if (seleccionada != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Nombre Edición: ").append(seleccionada.getIdNombre()).append("\n");
            sb.append("Sigla: ").append(seleccionada.getSigla()).append("\n");
            sb.append("Fecha Inicio: ").append(seleccionada.getFechaInicio()).append("\n");
            sb.append("Fecha Fin: ").append(seleccionada.getFechaFin()).append("\n");
            sb.append("Fecha Alta: ").append(seleccionada.getFechaAlta()).append("\n");
            sb.append("Ciudad: ").append(seleccionada.getCiudad()).append("\n");
            sb.append("País: ").append(seleccionada.getPais()).append("\n\n");

            sb.append("--- Tipos de Registro Disponibles ---\n");
            Collection<DtTipoRegistro> tipos = sistema.obtenerTiposRegistroEdicion(edicionSel);
            if (tipos.isEmpty()) {
                sb.append("No hay tipos de registro para esta edición.\n");
            } else {
                for (DtTipoRegistro tr : tipos) {
                    sb.append("- ").append(tr.getIdNombre())
                            .append(" | Costo: $").append(tr.getCosto())
                            .append(" | Cupo: ").append(tr.getCupo()).append("\n");
                }
            }
            areaDetalles.setText(sb.toString());
        }
    }
}