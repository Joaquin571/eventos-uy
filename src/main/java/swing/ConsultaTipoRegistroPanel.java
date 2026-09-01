package swing;

import datatypes.*;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConsultaTipoRegistroPanel extends JPanel {

    private final ISistema sistema;
    private JComboBox<String> comboEventos;
    private JComboBox<String> comboEdiciones;
    private JComboBox<String> comboTiposRegistro;

    private JLabel lblNombre;
    private JLabel lblDescripcion;
    private JLabel lblCosto;
    private JLabel lblCupo;

    public ConsultaTipoRegistroPanel() {
        this.sistema = Fabrica.getInstance().getISistema();
        armarUI();
        configurarEventos();
    }

    private void armarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de selección desplegable
        JPanel panelFiltros = new JPanel(new GridLayout(3, 2, 10, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Selección de Tipo de Registro"));

        panelFiltros.add(new JLabel("Evento:"));
        comboEventos = new JComboBox<>();
        panelFiltros.add(comboEventos);

        panelFiltros.add(new JLabel("Edición:"));
        comboEdiciones = new JComboBox<>();
        panelFiltros.add(comboEdiciones);

        panelFiltros.add(new JLabel("Tipo de Registro:"));
        comboTiposRegistro = new JComboBox<>();
        panelFiltros.add(comboTiposRegistro);

        // Panel para mostrar la información detallada
        JPanel panelDetalle = new JPanel(new GridLayout(4, 2, 10, 10));
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Datos del Tipo de Registro"));

        panelDetalle.add(new JLabel("Nombre:"));
        lblNombre = new JLabel("-");
        panelDetalle.add(lblNombre);

        panelDetalle.add(new JLabel("Descripción:"));
        lblDescripcion = new JLabel("-");
        panelDetalle.add(lblDescripcion);

        panelDetalle.add(new JLabel("Costo:"));
        lblCosto = new JLabel("-");
        panelDetalle.add(lblCosto);

        panelDetalle.add(new JLabel("Cupo:"));
        lblCupo = new JLabel("-");
        panelDetalle.add(lblCupo);

        add(panelFiltros, BorderLayout.NORTH);
        add(panelDetalle, BorderLayout.CENTER);
    }

    private void configurarEventos() {
        comboEventos.addActionListener(e -> cargarEdiciones());
        comboEdiciones.addActionListener(e -> cargarTiposRegistro());
        comboTiposRegistro.addActionListener(e -> mostrarDatosTipoRegistro());
    }

    public void cargarEventos() {
        comboEventos.removeAllItems();
        Collection<DtEvento> eventos = sistema.listarEventos();
        for (DtEvento ev : eventos) {
            comboEventos.addItem(ev.getNombre());
        }
        limpiarCampos();
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
        limpiarCampos();
    }

    private void cargarTiposRegistro() {
        comboTiposRegistro.removeAllItems();
        String edicionSel = (String) comboEdiciones.getSelectedItem();
        if (edicionSel != null) {
            Collection<DtTipoRegistro> tipos = sistema.obtenerTiposRegistroEdicion(edicionSel);
            for (DtTipoRegistro tr : tipos) {
                comboTiposRegistro.addItem(tr.getIdNombre());
            }
        }
        limpiarCampos();
    }

    private void mostrarDatosTipoRegistro() {
        String tipoSel = (String) comboTiposRegistro.getSelectedItem();
        if (tipoSel == null) {
            limpiarCampos();
            return;
        }

        DtTipoRegistro dtTR = sistema.consultarTipoRegistro(tipoSel);
        if (dtTR != null) {
            lblNombre.setText(dtTR.getIdNombre());
            lblDescripcion.setText(dtTR.getDescripcion());
            lblCosto.setText("$" + dtTR.getCosto());
            lblCupo.setText(String.valueOf(dtTR.getCupo()));
        } else {
            limpiarCampos();
        }
    }

    private void limpiarCampos() {
        lblNombre.setText("-");
        lblDescripcion.setText("-");
        lblCosto.setText("-");
        lblCupo.setText("-");
    }
}