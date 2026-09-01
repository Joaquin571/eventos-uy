package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtTipoRegistro;
import interfaces.ISistema;
import implementacion.Fabrica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AltaEdicionEventoPanel extends JPanel {

    private final ISistema sistema;


    private JComboBox<String> comboEventos;
    private JTextField txtNombreEdicion;
    private JTextField txtSigla;
    private JSpinner spinnerFechaInicio;
    private JSpinner spinnerFechaFin;
    private JTextField txtCiudad;
    private JTextField txtPais;

    // Componentes Tipo de Registro
    private JTextField txtNombreTR;
    private JTextField txtDescripcionTR;
    private JTextField txtCostoTR;
    private JTextField txtCupoTR;
    private JTable tablaTiposRegistro;
    private DefaultTableModel modelTablaTR;

    private final List<DtTipoRegistro> listaTiposRegistroAgregados = new ArrayList<>();

    public AltaEdicionEventoPanel() {
        this.sistema = Fabrica.getInstance().getISistema();
        initUI();
        cargarEventos();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel de Formulario Principal
        JPanel panelForm = new JPanel(new GridLayout(7, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos de la Edición"));

        panelForm.add(new JLabel("Seleccionar Evento:"));
        comboEventos = new JComboBox<>();
        panelForm.add(comboEventos);

        panelForm.add(new JLabel("Nombre / ID Edición:"));
        txtNombreEdicion = new JTextField();
        panelForm.add(txtNombreEdicion);

        panelForm.add(new JLabel("Sigla:"));
        txtSigla = new JTextField();
        panelForm.add(txtSigla);

        panelForm.add(new JLabel("Fecha Inicio:"));
        spinnerFechaInicio = new JSpinner(new SpinnerDateModel());
        spinnerFechaInicio.setEditor(new JSpinner.DateEditor(spinnerFechaInicio, "yyyy-MM-dd"));
        panelForm.add(spinnerFechaInicio);

        panelForm.add(new JLabel("Fecha Fin:"));
        spinnerFechaFin = new JSpinner(new SpinnerDateModel());
        spinnerFechaFin.setEditor(new JSpinner.DateEditor(spinnerFechaFin, "yyyy-MM-dd"));
        panelForm.add(spinnerFechaFin);

        panelForm.add(new JLabel("Ciudad:"));
        txtCiudad = new JTextField();
        panelForm.add(txtCiudad);

        panelForm.add(new JLabel("País:"));
        txtPais = new JTextField();
        panelForm.add(txtPais);

        // Panel de Tipos de Registro
        JPanel panelTR = new JPanel(new BorderLayout(5, 5));
        panelTR.setBorder(BorderFactory.createTitledBorder("Tipos de Registro de la Edición"));

        JPanel panelInputsTR = new JPanel(new GridLayout(2, 4, 5, 5));
        txtNombreTR = new JTextField();
        txtDescripcionTR = new JTextField();
        txtCostoTR = new JTextField();
        txtCupoTR = new JTextField();

        panelInputsTR.add(new JLabel("Nombre:"));
        panelInputsTR.add(new JLabel("Descripción:"));
        panelInputsTR.add(new JLabel("Costo ($):"));
        panelInputsTR.add(new JLabel("Cupo:"));

        panelInputsTR.add(txtNombreTR);
        panelInputsTR.add(txtDescripcionTR);
        panelInputsTR.add(txtCostoTR);
        panelInputsTR.add(txtCupoTR);

        JButton btnAgregarTR = new JButton("Agregar Tipo Registro");
        btnAgregarTR.addActionListener(e -> agregarTipoRegistroALista());

        JPanel panelNorthTR = new JPanel(new BorderLayout());
        panelNorthTR.add(panelInputsTR, BorderLayout.CENTER);
        panelNorthTR.add(btnAgregarTR, BorderLayout.SOUTH);

        modelTablaTR = new DefaultTableModel(new String[]{"Nombre", "Descripción", "Costo", "Cupo"}, 0);
        tablaTiposRegistro = new JTable(modelTablaTR);
        JScrollPane scrollTabla = new JScrollPane(tablaTiposRegistro);

        panelTR.add(panelNorthTR, BorderLayout.NORTH);
        panelTR.add(scrollTabla, BorderLayout.CENTER);

        // Panel Central que junta Edición + Tipos de Registro
        JPanel panelCenter = new JPanel(new BorderLayout(10, 10));
        panelCenter.add(panelForm, BorderLayout.NORTH);
        panelCenter.add(panelTR, BorderLayout.CENTER);

        // Botón Guardar
        JButton btnGuardar = new JButton("Alta Edición de Evento");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnGuardar.addActionListener(e -> guardarEdicion());

        add(panelCenter, BorderLayout.CENTER);
        add(btnGuardar, BorderLayout.SOUTH);
    }

    public void cargarEventos() {
        comboEventos.removeAllItems();
        Collection<DtEvento> eventos = sistema.listarEventos();
        for (DtEvento ev : eventos) {
            comboEventos.addItem(ev.getNombre());
        }
    }

    private void agregarTipoRegistroALista() {
        String nombre = txtNombreTR.getText().trim();
        String desc = txtDescripcionTR.getText().trim();
        String costoStr = txtCostoTR.getText().trim();
        String cupoStr = txtCupoTR.getText().trim();

        if (nombre.isEmpty() || desc.isEmpty() || costoStr.isEmpty() || cupoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos del Tipo de Registro.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            float costo = Float.parseFloat(costoStr);
            int cupo = Integer.parseInt(cupoStr);

            DtTipoRegistro dtTR = new DtTipoRegistro(nombre, desc, costo, cupo);
            listaTiposRegistroAgregados.add(dtTR);
            modelTablaTR.addRow(new Object[]{nombre, desc, costo, cupo});

            // Limpiar inputs TR
            txtNombreTR.setText("");
            txtDescripcionTR.setText("");
            txtCostoTR.setText("");
            txtCupoTR.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Costo y Cupo deben ser valores numéricos válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarEdicion() {
        String eventoSel = (String) comboEventos.getSelectedItem();
        String nombre = txtNombreEdicion.getText().trim();
        String sigla = txtSigla.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String pais = txtPais.getText().trim();

        if (eventoSel == null || nombre.isEmpty() || sigla.isEmpty() || ciudad.isEmpty() || pais.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos de la edición.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fInicio = toLocalDate((Date) spinnerFechaInicio.getValue());
        LocalDate fFin = toLocalDate((Date) spinnerFechaFin.getValue());

        if (fFin.isBefore(fInicio)) {
            JOptionPane.showMessageDialog(this, "La fecha de fin no puede ser anterior a la de inicio.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DtEdicion dtEdicion = new DtEdicion(nombre, sigla, fInicio, fFin, LocalDate.now(), ciudad, pais);

        try {
            boolean okEdicion = sistema.altaEdicion(dtEdicion, eventoSel);

            if (okEdicion) {
                for (DtTipoRegistro dtTR : listaTiposRegistroAgregados) {
                    sistema.altaTipoRegistro(dtTR, dtEdicion.getIdNombre());
                }
                JOptionPane.showMessageDialog(this, "Edición registrada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear la edición (ya existe una con ese nombre).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void limpiarFormulario() {
        txtNombreEdicion.setText("");
        txtSigla.setText("");
        txtCiudad.setText("");
        txtPais.setText("");
        listaTiposRegistroAgregados.clear();
        modelTablaTR.setRowCount(0);
    }
}
