package swing;

import datatypes.DtEdicion;
import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import interfaces.ISistema;
import implementacion.Fabrica;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

public class AltaEdicionEventoPanel extends JPanel {

    private final ISistema sistema;
    private final ImagenSwing imagen = new ImagenSwing(true);

    private JComboBox<String> comboEventos;
    private JComboBox<String> comboOrganizadores;

    private JTextField txtNombreEdicion;
    private JTextField txtSigla;
    private JSpinner spinnerFechaInicio;
    private JSpinner spinnerFechaFin;
    private JTextField txtCiudad;
    private JTextField txtPais;

    public AltaEdicionEventoPanel() {

        this.sistema = Fabrica.getInstance().getISistema();

        initUI();

        cargarEventos();
        cargarOrganizadores();
    }

    private void initUI() {

        setLayout(new BorderLayout(10, 10));

        // =====================================================
        // DATOS DE LA EDICIÓN
        // =====================================================

        JPanel panelForm = new JPanel(new GridLayout(8, 2, 5, 5));

        panelForm.setBorder(
                BorderFactory.createTitledBorder(
                        "Datos de la Edición"
                )
        );

        panelForm.add(new JLabel("Seleccionar Evento:"));
        comboEventos = new JComboBox<>();
        panelForm.add(comboEventos);

        panelForm.add(new JLabel("Organizador:"));
        comboOrganizadores = new JComboBox<>();
        panelForm.add(comboOrganizadores);

        panelForm.add(new JLabel("Nombre / ID Edición:"));
        txtNombreEdicion = new JTextField();
        panelForm.add(txtNombreEdicion);

        panelForm.add(new JLabel("Sigla:"));
        txtSigla = new JTextField();
        panelForm.add(txtSigla);

        panelForm.add(new JLabel("Fecha Inicio (AAAA-MM-DD):"));
        spinnerFechaInicio = new JSpinner(new SpinnerDateModel());
        spinnerFechaInicio.setEditor(
                new JSpinner.DateEditor(
                        spinnerFechaInicio,
                        "yyyy-MM-dd"
                )
        );
        panelForm.add(spinnerFechaInicio);

        panelForm.add(new JLabel("Fecha Fin (AAAA-MM-DD):"));
        spinnerFechaFin = new JSpinner(new SpinnerDateModel());
        spinnerFechaFin.setEditor(
                new JSpinner.DateEditor(
                        spinnerFechaFin,
                        "yyyy-MM-dd"
                )
        );
        panelForm.add(spinnerFechaFin);

        panelForm.add(new JLabel("Ciudad:"));
        txtCiudad = new JTextField();
        panelForm.add(txtCiudad);

        panelForm.add(new JLabel("País:"));
        txtPais = new JTextField();
        panelForm.add(txtPais);

        // =====================================================
        // BOTÓN GUARDAR
        // =====================================================

        JButton btnGuardar = new JButton("Confirmar");
        btnGuardar.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        btnGuardar.addActionListener(
                e -> guardarEdicion()
        );

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(panelForm, BorderLayout.NORTH);
        contenido.add(imagen, BorderLayout.CENTER);
        add(new JScrollPane(contenido), BorderLayout.CENTER);
        add(btnGuardar, BorderLayout.SOUTH);
    }

    // =====================================================
    // CARGAR EVENTOS
    // =====================================================

    public void cargarEventos() {

        comboEventos.removeAllItems();

        Collection<DtEvento> eventos = sistema.listarEventos();

        for (DtEvento evento : eventos) {
            comboEventos.addItem(
                    evento.getNombre()
            );
        }
    }

    // =====================================================
    // CARGAR ORGANIZADORES
    // =====================================================

    public void cargarOrganizadores() {

        comboOrganizadores.removeAllItems();

        Collection<DtUsuario> usuarios = sistema.listarUsuarios();

        for (DtUsuario usuario : usuarios) {

            if (usuario instanceof DtOrganizador) {

                comboOrganizadores.addItem(
                        usuario.getNickname()
                );
            }
        }
    }

    // =====================================================
    // GUARDAR EDICIÓN
    // =====================================================

    private void guardarEdicion() {
        if (imagen.reintentarAlta(sistema, this::limpiarFormulario)) return;

        String eventoSel = (String) comboEventos.getSelectedItem();
        String organizadorSel = (String) comboOrganizadores.getSelectedItem();
        String nombre = txtNombreEdicion.getText().trim();
        String sigla = txtSigla.getText().trim();
        String ciudad = txtCiudad.getText().trim();
        String pais = txtPais.getText().trim();

        if (eventoSel == null
                || organizadorSel == null
                || nombre.isEmpty()
                || sigla.isEmpty()
                || ciudad.isEmpty()
                || pais.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Por favor complete todos los campos de la edición.",
                    "Atención",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        LocalDate fInicio = toLocalDate((Date) spinnerFechaInicio.getValue());
        LocalDate fFin = toLocalDate((Date) spinnerFechaFin.getValue());

        if (fFin.isBefore(fInicio)) {

            JOptionPane.showMessageDialog(
                    this,
                    "La fecha de fin no puede ser anterior a la de inicio.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        DtEdicion dtEdicion = new DtEdicion(
                nombre,
                sigla,
                fInicio,
                fFin,
                LocalDate.now(),
                ciudad,
                pais,
                organizadorSel
        );

        try {

            DtImagen nuevaImagen = imagen.leerValidada(sistema);
            boolean okEdicion = sistema.altaEdicion(dtEdicion, eventoSel);

            if (okEdicion) {
                if (!imagen.completarAlta(sistema, TipoEntidadImagen.EDICION, nombre, nuevaImagen)) return;

                JOptionPane.showMessageDialog(
                        this,
                        "Edición registrada correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();

                JInternalFrame internalFrame = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, this);
                if (internalFrame != null) {
                    internalFrame.setVisible(false); // Oculta la ventana sin destruirla
                }

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo crear la edición. Verifique que el nombre no exista y que el organizador sea válido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDate toLocalDate(Date date) {

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void limpiarFormulario() {
        imagen.mostrar(null);

        txtNombreEdicion.setText("");
        txtSigla.setText("");
        txtCiudad.setText("");
        txtPais.setText("");
    }
}