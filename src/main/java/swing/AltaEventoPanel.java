package swing;

import datatypes.DtEvento;
import interfaces.ISistema;
import implementacion.Fabrica;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AltaEventoPanel {

    private JPanel mainPanel;
    private JTextField txtNombre;
    private JTextField txtSigla;
    private JTextField txtDescripcion;
    private JButton btnSeleccionarCategorias;
    private JPopupMenu popupCategorias;
    private List<JCheckBox> checkCategorias;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private Runnable accionCerrar;

    public AltaEventoPanel() {
        checkCategorias = new ArrayList<>();
        popupCategorias = new JPopupMenu();

        armarUI();
        cargarCategorias();

        btnAceptar.addActionListener(e -> agregarEvento());
        btnCancelar.addActionListener(e -> {
            limpiarCampos();
            if (accionCerrar != null) accionCerrar.run();
        });
    }

    private void armarUI() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        mainPanel.add(txtNombre, gbc);

        // Sigla
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Sigla:"), gbc);
        gbc.gridx = 1;
        txtSigla = new JTextField(20);
        mainPanel.add(txtSigla, gbc);

        // Descripción
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        txtDescripcion = new JTextField(20);
        mainPanel.add(txtDescripcion, gbc);

        // Categorías (Botón desplegable con casillas de selección)
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Categorías:"), gbc);
        gbc.gridx = 1;
        btnSeleccionarCategorias = new JButton("Seleccionar categorías...");
        btnSeleccionarCategorias.addActionListener(e -> {
            if (checkCategorias.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "No hay categorías registradas.", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                popupCategorias.show(btnSeleccionarCategorias, 0, btnSeleccionarCategorias.getHeight());
            }
        });
        mainPanel.add(btnSeleccionarCategorias, gbc);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAceptar = new JButton("Aceptar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(panelBotones, gbc);
    }

    public void cargarCategorias() {
        popupCategorias.removeAll();
        checkCategorias.clear();

        ISistema sistema = Fabrica.getInstance().getISistema();

        // Iterar directamente sobre la Collection/Set devuelta por el sistema
        for (String cat : sistema.listarCategorias()) {
            JCheckBox chk = new JCheckBox(cat);
            checkCategorias.add(chk);
            popupCategorias.add(chk);
        }
    }

    private void agregarEvento() {
        String nombre = txtNombre.getText().trim();
        String sigla = txtSigla.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        // Obtener todas las categorías marcadas por el usuario
        Set<String> categoriasSeleccionadas = new HashSet<>();
        for (JCheckBox chk : checkCategorias) {
            if (chk.isSelected()) {
                categoriasSeleccionadas.add(chk.getText());
            }
        }

        if (nombre.isEmpty() || sigla.isEmpty() || descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Debe completar todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (categoriasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Debe seleccionar al menos una categoría.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DtEvento dt = new DtEvento(nombre, sigla, descripcion, LocalDate.now(), categoriasSeleccionadas);

            ISistema sistema = Fabrica.getInstance().getISistema();
            sistema.altaEvento(dt);

            JOptionPane.showMessageDialog(mainPanel, "Evento registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            if (accionCerrar != null) accionCerrar.run();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error en Alta Evento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtSigla.setText("");
        txtDescripcion.setText("");
        for (JCheckBox chk : checkCategorias) {
            chk.setSelected(false);
        }
    }

    public JPanel getMainPanel() { return mainPanel; }
    public void setAccionCerrar(Runnable accionCerrar) { this.accionCerrar = accionCerrar; }
}