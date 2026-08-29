package swing;

import datatypes.*;
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
    }

    private void armarUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 10, 10));

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

        btnRegistrar = new JButton("Confirmar Registro");
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.add(btnRegistrar);

        add(panelForm, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    private void configurarEventos() {
        comboEventos.addActionListener(e -> cargarEdiciones());
        comboEdiciones.addActionListener(e -> cargarTiposRegistro());

        btnRegistrar.addActionListener(e -> ejecutarRegistro());
    }

    public void cargarAsistentes() {
        comboAsistentes.removeAllItems();
        Collection<DtUsuario> usuarios = sistema.listarUsuarios();
        for (DtUsuario u : usuarios) {
            if (u instanceof DtAsistente) {
                comboAsistentes.addItem(u.getNickname());
            }
        }
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

    private void cargarTiposRegistro() {
        comboTiposRegistro.removeAllItems();
        String edicionSel = (String) comboEdiciones.getSelectedItem();
        if (edicionSel != null) {
            Collection<DtTipoRegistro> tipos = sistema.obtenerTiposRegistroEdicion(edicionSel);
            for (DtTipoRegistro tr : tipos) {
                comboTiposRegistro.addItem(tr.getIdNombre());
            }
        }
    }

    private void ejecutarRegistro() {
        String nickname = (String) comboAsistentes.getSelectedItem();
        String edicionSel = (String) comboEdiciones.getSelectedItem();
        String tipoRegistroSel = (String) comboTiposRegistro.getSelectedItem();

        if (nickname == null || edicionSel == null || tipoRegistroSel == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un asistente, un evento, una edición y un tipo de registro.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Validar si ya está registrado
        if (sistema.estaRegistradoAEdicion(nickname, edicionSel)) {
            JOptionPane.showMessageDialog(this, "El asistente seleccionado ya está registrado a esta edición.", "Registro duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Declaración única de dtTR
        DtTipoRegistro dtTR = sistema.consultarTipoRegistro(tipoRegistroSel);

        // 2. Validar si se alcanzó el cupo disponible
        if (dtTR != null && dtTR.getCupo() <= 0) {
            JOptionPane.showMessageDialog(this, "Se ha alcanzado el cupo máximo para este tipo de registro.", "Cupo agotado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Ejecutar alta de registro
        try {
            LocalDate fechaActual = LocalDate.now();
            float costo = (dtTR != null) ? dtTR.getCosto() : 0f;

            // Se reasigna directamente sin volver a declarar dtTR
            DtRegistro dtRegistro = new DtRegistro(fechaActual, costo);

            boolean ok = sistema.registroAEdicion(nickname, edicionSel, tipoRegistroSel, dtRegistro);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Registro realizado con éxito a la edición.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo completar el registro.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}