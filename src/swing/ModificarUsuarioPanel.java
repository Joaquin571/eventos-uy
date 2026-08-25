package swing;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ModificarUsuarioPanel {

    private JPanel mainPanel;

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    // Datos comunes
    private JPanel panelDatosComunes;
    private JComboBox<String> comboUsuarios;
    private JTextField txtNickname;
    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JComboBox<String> comboTipoUsuario;

    // Asistente
    private JPanel panelAsistente;
    private JTextField txtApellido;
    private JTextField txtFechaNacimiento;
    private JComboBox<String> comboInstitucion;

    // Organizador
    private JPanel panelOrganizador;
    private JTextArea txtDescripcion;
    private JTextField txtSitioWeb;

    // Botones
    private JPanel panelBotones;
    private JButton btnGuardar;
    private JButton btnCancelar;


    public ModificarUsuarioPanel() {

        // La presentación obtiene la lógica mediante la fábrica
        sistema = Fabrica.getInstance().getISistema();

        configurarTipoUsuario();
        configurarEventos();

        // Carga los usuarios existentes en el combo
        cargarUsuarios();

        actualizarCamposTipo();
    }


    /**
     * Carga los tipos de usuario.
     */
    private void configurarTipoUsuario() {

        comboTipoUsuario.removeAllItems();

        comboTipoUsuario.addItem("Asistente");
        comboTipoUsuario.addItem("Organizador");
    }


    /**
     * Carga los usuarios existentes en el JComboBox.
     */
    private void cargarUsuarios() {

        comboUsuarios.removeAllItems();

        for (DtUsuario usuario : sistema.listarUsuarios()) {
            comboUsuarios.addItem(usuario.getNickname());
        }

        // Arranca sin ningún usuario seleccionado
        comboUsuarios.setSelectedIndex(-1);
    }

    private void configurarEventos() {

        comboTipoUsuario.addActionListener(
                e -> actualizarCamposTipo()
        );

        comboUsuarios.addActionListener(
                e -> seleccionarUsuario()
        );

        btnGuardar.addActionListener(
                e -> guardarCambios()
        );

        btnCancelar.addActionListener(e -> {
            limpiarDatosEspecificos();
            accionCerrar.run();
        });
    }

    private void actualizarCamposTipo() {

        String tipoSeleccionado =
                (String) comboTipoUsuario.getSelectedItem();

        boolean esAsistente =
                "Asistente".equals(tipoSeleccionado);

        panelAsistente.setVisible(esAsistente);
        panelOrganizador.setVisible(!esAsistente);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void seleccionarUsuario() {

        String nickname =
                (String) comboUsuarios.getSelectedItem();

        if (nickname == null) {
            return;
        }

        DtUsuario usuario =
                sistema.consultarUsuario(nickname);

        if (usuario == null) {
            return;
        }

        cargarUsuario(usuario);
    }

    private void cargarUsuario(DtUsuario usuario) {

        cargarDatosComunes(
                usuario.getNickname(),
                usuario.getNombre(),
                usuario.getCorreoElectronico()
        );

        if (usuario instanceof DtAsistente) {

            DtAsistente asistente =
                    (DtAsistente) usuario;

            cargarAsistente(
                    asistente.getApellido(),
                    asistente.getFechaNacimiento().toString(),
                    null
            );

        } else if (usuario instanceof DtOrganizador) {

            DtOrganizador organizador =
                    (DtOrganizador) usuario;

            cargarOrganizador(
                    organizador.getDescripcion(),
                    organizador.getSitioWeb()
            );
        }
    }

    private void cargarDatosComunes(
            String nickname,
            String nombre,
            String correo) {

        txtNickname.setText(nickname);
        txtNombre.setText(nombre);
        txtCorreo.setText(correo);

        txtNickname.setEditable(false);
    }

    private void cargarAsistente(
            String apellido,
            String fechaNacimiento,
            String institucion) {

        comboTipoUsuario.setSelectedItem("Asistente");

        txtApellido.setText(apellido);
        txtFechaNacimiento.setText(fechaNacimiento);

        if (institucion != null) {
            comboInstitucion.setSelectedItem(institucion);
        }

        actualizarCamposTipo();
    }

    private void cargarOrganizador(
            String descripcion,
            String sitioWeb) {

        comboTipoUsuario.setSelectedItem("Organizador");

        txtDescripcion.setText(descripcion);
        txtSitioWeb.setText(sitioWeb);

        actualizarCamposTipo();
    }

    private void guardarCambios() {

        String nickname =
                txtNickname.getText().trim();

        String nombre =
                txtNombre.getText().trim();

        String correo =
                txtCorreo.getText().trim();

        String tipo =
                (String) comboTipoUsuario.getSelectedItem();


        if (nickname.isEmpty()
                || nombre.isEmpty()
                || correo.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Complete los datos obligatorios.",
                    "Modificar usuario",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        if ("Asistente".equals(tipo)) {

            guardarAsistente(
                    nickname,
                    nombre,
                    correo
            );

        } else if ("Organizador".equals(tipo)) {

            guardarOrganizador(
                    nickname,
                    nombre,
                    correo
            );
        }
    }


    /**
     * Guarda la modificación de un asistente.
     */
    private void guardarAsistente(
            String nickname,
            String nombre,
            String correo) {

        String apellido =
                txtApellido.getText().trim();

        String fechaTexto =
                txtFechaNacimiento.getText().trim();

        String institucion =
                (String) comboInstitucion.getSelectedItem();


        if (apellido.isEmpty() || fechaTexto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Complete apellido y fecha de nacimiento.",
                    "Modificar usuario",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        try {

            LocalDate fechaNacimiento =
                    LocalDate.parse(fechaTexto);

            DtAsistente dt =
                    new DtAsistente(
                            nickname,
                            nombre,
                            correo,
                            apellido,
                            fechaNacimiento
                    );

            sistema.modificarAsistente(
                    dt,
                    institucion
            );

            mostrarExito();

        } catch (DateTimeParseException e) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "La fecha debe tener formato AAAA-MM-DD.",
                    "Modificar usuario",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }


    /**
     * Guarda la modificación de un organizador.
     */
    private void guardarOrganizador(
            String nickname,
            String nombre,
            String correo) {

        String descripcion =
                txtDescripcion.getText().trim();

        String sitioWeb =
                txtSitioWeb.getText().trim();


        DtOrganizador dt =
                new DtOrganizador(
                        nickname,
                        nombre,
                        correo,
                        descripcion,
                        sitioWeb
                );


        sistema.modificarOrganizador(dt);

        mostrarExito();
    }


    /**
     * Mensaje después de modificar correctamente.
     */
    private void mostrarExito() {

        JOptionPane.showMessageDialog(
                mainPanel,
                "Usuario modificado correctamente.",
                "Modificar usuario",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Limpia los campos específicos.
     */
    private void limpiarDatosEspecificos() {

        txtApellido.setText("");
        txtFechaNacimiento.setText("");

        if (comboInstitucion.getItemCount() > 0) {
            comboInstitucion.setSelectedIndex(-1);
        }

        txtDescripcion.setText("");
        txtSitioWeb.setText("");
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    /**
     * Principal define qué hacer cuando se presiona Cancelar.
     */
    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
}