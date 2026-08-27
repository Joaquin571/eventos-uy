package swing;

import datatypes.DtAsistente;
import datatypes.DtInstitucion;
import datatypes.DtOrganizador;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

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
    private JTextField txtFechaNac;
    private JComboBox<String> comboInstitucion;

    // Campos para Organizador
    private JTextArea txtDescripcion;
    private JTextField txtSitioWeb;

    // Botones
    private JButton btnAceptar;
    private JButton btnCancelar;

    // Paneles dinámicos
    private JPanel panelAsistente;
    private JPanel panelOrganizador;

    private Runnable accionCerrar = () -> {};


    public AltaUsuarioPanel() {

        sistema = Fabrica.getInstance().getISistema();

        inicializarComponentes();

        // Cargar instituciones existentes
        cargarInstituciones();

        radioAsistente.addActionListener(
                e -> cambiarTipoUsuario(true)
        );

        radioOrganizador.addActionListener(
                e -> cambiarTipoUsuario(false)
        );

        btnAceptar.addActionListener(
                e -> registrarUsuario()
        );

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


    /**
     * Carga las instituciones existentes desde ISistema.
     */
    public void cargarInstituciones() {

        comboInstitucion.removeAllItems();

        // El asistente puede no pertenecer a ninguna institución
        comboInstitucion.addItem("Sin Institución");

        for (DtInstitucion institucion : sistema.listarInstituciones()) {
            comboInstitucion.addItem(institucion.getNombre());
        }

        comboInstitucion.setSelectedIndex(0);
    }


    /**
     * Muestra únicamente los campos correspondientes
     * al tipo de usuario seleccionado.
     */
    private void cambiarTipoUsuario(boolean esAsistente) {

        panelAsistente.setVisible(esAsistente);
        panelOrganizador.setVisible(!esAsistente);

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    /**
     * Ejecuta el Alta de Usuario.
     */
    private void registrarUsuario() {

        String nickname =
                txtNickname.getText().trim();

        String nombre =
                txtNombre.getText().trim();

        String correo =
                txtCorreo.getText().trim();


        // ========================================
        // VALIDACIONES COMUNES
        // ========================================

        if (nickname.isEmpty()
                || nombre.isEmpty()
                || correo.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Complete los campos obligatorios: Nickname, Nombre y Correo.",
                    "Alta Usuario",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        if (!correo.contains("@")
                || correo.indexOf('@') == 0
                || correo.indexOf('@') == correo.length() - 1) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Ingrese un correo electrónico válido.",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        if (sistema.existeUsuario(nickname)) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El nickname '" + nickname + "' ya existe en el sistema.",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        if (sistema.existeCorreoElectronico(correo)) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El correo electrónico '" + correo
                            + "' ya se encuentra registrado.",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        // ========================================
        // ASISTENTE
        // ========================================

        if (radioAsistente.isSelected()) {

            String apellido =
                    txtApellido.getText().trim();

            String fechaTexto =
                    txtFechaNac.getText().trim();

            String nombreInstitucion =
                    (String) comboInstitucion.getSelectedItem();


            if (apellido.isEmpty()) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "El apellido es obligatorio para asistentes.",
                        "Alta Usuario",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }


            if (fechaTexto.isEmpty()) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "La fecha de nacimiento es obligatoria.",
                        "Alta Usuario",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }


            LocalDate fechaNac;

            try {

                if (!fechaTexto.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new IllegalArgumentException();
                }

                fechaNac =
                        LocalDate.parse(fechaTexto);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "Formato de fecha inválido. Debe usar AAAA-MM-DD.",
                        "Error de Formato",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }


            if (!fechaNac.isBefore(LocalDate.now())) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "La fecha de nacimiento debe ser anterior a la fecha actual.",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }


            /*
             * Si eligió "Sin Institución", mandamos null.
             */
            if ("Sin Institución".equals(nombreInstitucion)) {
                nombreInstitucion = null;
            }


            DtAsistente dtAsistente =
                    new DtAsistente(
                            nickname,
                            nombre,
                            correo,
                            apellido,
                            fechaNac,
                            nombreInstitucion
                    );


            boolean agregado =
                    sistema.altaAsistente(dtAsistente);


            if (!agregado) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No se pudo registrar el usuario.",
                        "Alta Usuario",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }


            // ========================================
            // ORGANIZADOR
            // ========================================

        } else {

            String descripcion =
                    txtDescripcion.getText().trim();

            String sitioWeb =
                    txtSitioWeb.getText().trim();


            if (descripcion.isEmpty()) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "La descripción es obligatoria para organizadores.",
                        "Alta Usuario",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }


            DtOrganizador dtOrganizador =
                    new DtOrganizador(
                            nickname,
                            nombre,
                            correo,
                            descripcion,
                            sitioWeb.isEmpty()
                                    ? null
                                    : sitioWeb
                    );


            boolean agregado =
                    sistema.altaOrganizador(dtOrganizador);


            if (!agregado) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No se pudo registrar el usuario.",
                        "Alta Usuario",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }
        }


        // ========================================
        // ÉXITO
        // ========================================

        JOptionPane.showMessageDialog(
                mainPanel,
                "Usuario registrado con éxito.",
                "Alta Usuario",
                JOptionPane.INFORMATION_MESSAGE
        );

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


    /**
     * Construcción de los componentes Swing.
     */
    private void inicializarComponentes() {

        mainPanel =
                new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );


        // ========================================
        // TIPO USUARIO
        // ========================================

        JPanel panelTipo =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        panelTipo.setBorder(
                BorderFactory.createTitledBorder(
                        "Tipo de Usuario"
                )
        );


        radioAsistente =
                new JRadioButton(
                        "Asistente",
                        true
                );

        radioOrganizador =
                new JRadioButton(
                        "Organizador"
                );


        bgTipoUsuario =
                new ButtonGroup();

        bgTipoUsuario.add(
                radioAsistente
        );

        bgTipoUsuario.add(
                radioOrganizador
        );


        panelTipo.add(
                radioAsistente
        );

        panelTipo.add(
                radioOrganizador
        );


        // ========================================
        // FORMULARIO
        // ========================================

        JPanel panelForm =
                new JPanel();

        panelForm.setLayout(
                new BoxLayout(
                        panelForm,
                        BoxLayout.Y_AXIS
                )
        );


        // ========================================
        // DATOS GENERALES
        // ========================================

        JPanel panelComun =
                new JPanel(
                        new GridLayout(
                                3,
                                2,
                                5,
                                5
                        )
                );

        panelComun.setBorder(
                BorderFactory.createTitledBorder(
                        "Datos Generales"
                )
        );


        txtNickname =
                new JTextField();

        txtNombre =
                new JTextField();

        txtCorreo =
                new JTextField();


        panelComun.add(
                new JLabel("Nickname (*):")
        );

        panelComun.add(
                txtNickname
        );

        panelComun.add(
                new JLabel("Nombre (*):")
        );

        panelComun.add(
                txtNombre
        );

        panelComun.add(
                new JLabel("Correo (*):")
        );

        panelComun.add(
                txtCorreo
        );


        // ========================================
        // ASISTENTE
        // ========================================

        panelAsistente =
                new JPanel(
                        new GridLayout(
                                3,
                                2,
                                5,
                                5
                        )
                );

        panelAsistente.setBorder(
                BorderFactory.createTitledBorder(
                        "Detalles del Asistente"
                )
        );


        txtApellido =
                new JTextField();

        txtFechaNac =
                new JTextField();

        txtFechaNac.setToolTipText(
                "Ejemplo: 1999-09-09"
        );


        comboInstitucion =
                new JComboBox<>();


        panelAsistente.add(
                new JLabel("Apellido (*):")
        );

        panelAsistente.add(
                txtApellido
        );

        panelAsistente.add(
                new JLabel(
                        "Fecha Nacimiento (AAAA-MM-DD) (*):"
                )
        );

        panelAsistente.add(
                txtFechaNac
        );

        panelAsistente.add(
                new JLabel("Institución:")
        );

        panelAsistente.add(
                comboInstitucion
        );


        // ========================================
        // ORGANIZADOR
        // ========================================

        panelOrganizador =
                new JPanel(
                        new GridBagLayout()
                );

        panelOrganizador.setBorder(
                BorderFactory.createTitledBorder(
                        "Detalles del Organizador"
                )
        );


        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(5, 5, 5, 5);

        gbc.fill =
                GridBagConstraints.BOTH;


        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        panelOrganizador.add(
                new JLabel("Descripción (*):"),
                gbc
        );


        txtDescripcion =
                new JTextArea(4, 25);

        txtDescripcion.setLineWrap(true);

        txtDescripcion.setWrapStyleWord(true);


        JScrollPane scrollDesc =
                new JScrollPane(
                        txtDescripcion
                );

        scrollDesc.setPreferredSize(
                new Dimension(
                        220,
                        80
                )
        );


        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        panelOrganizador.add(
                scrollDesc,
                gbc
        );


        // Sitio web
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        panelOrganizador.add(
                new JLabel("Sitio Web:"),
                gbc
        );


        txtSitioWeb =
                new JTextField();

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        panelOrganizador.add(
                txtSitioWeb,
                gbc
        );


        // ========================================
        // AGREGAR SUBPANELES
        // ========================================

        panelForm.add(
                panelComun
        );

        panelForm.add(
                Box.createVerticalStrut(10)
        );

        panelForm.add(
                panelAsistente
        );

        panelForm.add(
                panelOrganizador
        );


        // ========================================
        // BOTONES
        // ========================================

        JPanel panelBotones =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        btnCancelar =
                new JButton("Cancelar");

        btnAceptar =
                new JButton("Aceptar");

        panelBotones.add(
                btnCancelar
        );

        panelBotones.add(
                btnAceptar
        );


        // ========================================
        // PANEL PRINCIPAL
        // ========================================

        mainPanel.add(
                panelTipo,
                BorderLayout.NORTH
        );

        mainPanel.add(
                panelForm,
                BorderLayout.CENTER
        );

        mainPanel.add(
                panelBotones,
                BorderLayout.SOUTH
        );
    }
}