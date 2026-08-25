package swing;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Contenido de la ventana interna "Alta de Usuario".
 */
public class AltaUsuarioPanel {

    private JPanel mainPanel;

    private final transient ISistema sistema;
    // Componentes comunes
    private JRadioButton radioAsistente;
    private JRadioButton radioOrganizador;
    private ButtonGroup bgTipoUsuario;
    private JTextField txtNickname;
    private JTextField txtNombre;
    private JTextField txtCorreo;

    // Campos para Asistente
    private JTextField txtApellido;
    private JTextField txtFechaNac; // Texto manual para la fecha
    private JComboBox<String> comboInstitucion;

    // Campos para Organizador
    private JTextArea txtDescripcion; // Área de texto ampliada
    private JTextField txtSitioWeb;

    // Botones de acción
    private JButton btnAceptar;
    private JButton btnCancelar;

    // Paneles dinámicos
    private JPanel panelAsistente;
    private JPanel panelOrganizador;

    private Runnable accionCerrar = () -> {};

    public AltaUsuarioPanel() {
        sistema = Fabrica.getInstance().getISistema();

        inicializarComponentes();

        // Eventos de los botones de radio
        radioAsistente.addActionListener(e -> cambiarTipoUsuario(true));
        radioOrganizador.addActionListener(e -> cambiarTipoUsuario(false));

        btnAceptar.addActionListener(e -> registrarUsuario());
        btnCancelar.addActionListener(e -> {
            limpiar();
            accionCerrar.run();
        });

        cambiarTipoUsuario(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    private void cambiarTipoUsuario(boolean esAsistente) {
        panelAsistente.setVisible(esAsistente);
        panelOrganizador.setVisible(!esAsistente);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void registrarUsuario() {
        String nickname = txtNickname.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();

        // 1. Validaciones de campos obligatorios básicos
        if (nickname.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Complete los campos obligatorios: Nickname, Nombre y Correo.", "Alta Usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación de formato de Correo electrónico para que tenga un @
        if (!correo.contains("@") || correo.indexOf('@') == 0 || correo.indexOf('@') == correo.length() - 1) {
            JOptionPane.showMessageDialog(mainPanel, "Ingrese un correo electrónico válido (debe incluir '@').", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación de existencia previa en el sistema
        if (sistema.existeUsuario(nickname, nombre, correo)) {
            JOptionPane.showMessageDialog(mainPanel, "El nickname '" + nickname + "' ya existe en el sistema.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (sistema.existeCorreoElectronico(correo)) {
            JOptionPane.showMessageDialog(mainPanel, "El correo electrónico '" + correo + "' ya se encuentra registrado.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Procesar según el tipo de usuario seleccionado
        if (radioAsistente.isSelected()) {
            String apellido = txtApellido.getText().trim();
            String fechaTexto = txtFechaNac.getText().trim();

            if (apellido.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "El apellido es obligatorio para asistentes.", "Alta Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (fechaTexto.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "La fecha de nacimiento es obligatoria (ejemplo: 1999-09-09).", "Alta Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validar formato estricto AAAA-MM-DD
            LocalDate fechaNac;
            try {
                if (!fechaTexto.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new IllegalArgumentException();
                }
                fechaNac = LocalDate.parse(fechaTexto);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Formato de fecha inválido. Debe usar el formato AAAA-MM-DD (ejemplo: 1999-09-09).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validación de la fecha de nacimiento que tiene que ser anterior al día de hoy
            if (!fechaNac.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(mainPanel, "La fecha de nacimiento debe ser anterior a la fecha actual.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear y dar de alta asistente
            DtAsistente dtAsistente = new DtAsistente(nickname, nombre, correo, apellido, fechaNac);
            sistema.altaAsistente(dtAsistente);

        } else {
            String descripcion = txtDescripcion.getText().trim();
            String sitioWeb = txtSitioWeb.getText().trim();

            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "La descripción es obligatoria para organizadores.", "Alta Usuario", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Crear y dar de alta organizador
            DtOrganizador dtOrganizador = new DtOrganizador(nickname, nombre, correo, descripcion, sitioWeb.isEmpty() ? null : sitioWeb);
            sistema.altaOrganizador(dtOrganizador);
        }

        // Éxito y limpieza
        JOptionPane.showMessageDialog(mainPanel, "Usuario registrado con éxito.", "Alta Usuario", JOptionPane.INFORMATION_MESSAGE);
        limpiar();
        accionCerrar.run();
    }

    private void limpiar() {
        txtNickname.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        txtApellido.setText("");
        txtFechaNac.setText("");
        txtDescripcion.setText("");
        txtSitioWeb.setText("");
        radioAsistente.setSelected(true);
        cambiarTipoUsuario(true);
        if (comboInstitucion.getItemCount() > 0) {
            comboInstitucion.setSelectedIndex(0);
        }
    }

    private void inicializarComponentes() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: RadioButtons
        JPanel panelTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTipo.setBorder(BorderFactory.createTitledBorder("Tipo de Usuario"));
        radioAsistente = new JRadioButton("Asistente", true);
        radioOrganizador = new JRadioButton("Organizador");
        bgTipoUsuario = new ButtonGroup();
        bgTipoUsuario.add(radioAsistente);
        bgTipoUsuario.add(radioOrganizador);
        panelTipo.add(radioAsistente);
        panelTipo.add(radioOrganizador);

        // Formulario
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));

        // Datos Generales
        JPanel panelComun = new JPanel(new GridLayout(3, 2, 5, 5));
        panelComun.setBorder(BorderFactory.createTitledBorder("Datos Generales"));

        txtNickname = new JTextField();
        txtNombre = new JTextField();
        txtCorreo = new JTextField();

        panelComun.add(new JLabel("Nickname (*):"));
        panelComun.add(txtNickname);
        panelComun.add(new JLabel("Nombre (*):"));
        panelComun.add(txtNombre);
        panelComun.add(new JLabel("Correo (*):"));
        panelComun.add(txtCorreo);

        // Subpanel Asistente
        panelAsistente = new JPanel(new GridLayout(3, 2, 5, 5));
        panelAsistente.setBorder(BorderFactory.createTitledBorder("Detalles del Asistente"));

        txtApellido = new JTextField();
        txtFechaNac = new JTextField();
        txtFechaNac.setToolTipText("Ejemplo: 1999-09-09");
        comboInstitucion = new JComboBox<>(new String[]{"Sin Institución"});

        panelAsistente.add(new JLabel("Apellido (*):"));
        panelAsistente.add(txtApellido);
        panelAsistente.add(new JLabel("Fecha Nacimiento (AAAA-MM-DD) (*):"));
        panelAsistente.add(txtFechaNac);
        panelAsistente.add(new JLabel("Institución:"));
        panelAsistente.add(comboInstitucion);

        // Subpanel Organizador
        panelOrganizador = new JPanel(new GridBagLayout());
        panelOrganizador.setBorder(BorderFactory.createTitledBorder("Detalles del Organizador"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Fila Descripción
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.weighty = 0.0;
        panelOrganizador.add(new JLabel("Descripción (*):"), gbc);

        txtDescripcion = new JTextArea(4, 25);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setPreferredSize(new Dimension(220, 80));

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        panelOrganizador.add(scrollDesc, gbc);

        // Fila Sitio Web
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.weighty = 0.0;
        panelOrganizador.add(new JLabel("Sitio Web:"), gbc);

        txtSitioWeb = new JTextField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.0;
        panelOrganizador.add(txtSitioWeb, gbc);

        panelForm.add(panelComun);
        panelForm.add(Box.createVerticalStrut(10));
        panelForm.add(panelAsistente);
        panelForm.add(panelOrganizador);

        // Panel Inferior: Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar = new JButton("Cancelar");
        btnAceptar = new JButton("Aceptar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);

        mainPanel.add(panelTipo, BorderLayout.NORTH);
        mainPanel.add(panelForm, BorderLayout.CENTER);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);
    }
}