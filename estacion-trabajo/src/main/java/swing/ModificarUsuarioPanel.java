package swing;

import datatypes.DtAsistente;
import datatypes.DtImagen;
import datatypes.TipoEntidadImagen;
import java.io.IOException;
import java.util.Arrays;
import datatypes.DtInstitucion;
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

    private JPasswordField txtContrasenia;
    private JPasswordField txtConfirmacion;
    private ImagenSwing imagen;
    private DtUsuario usuarioSeleccionado;

    // Botones
    private JPanel panelBotones;
    private JButton btnGuardar;
    private JButton btnCancelar;


    public ModificarUsuarioPanel() {
        sistema = Fabrica.getInstance().getISistema();
    }

    /**
     * Asegura que los componentes gráficos no sean null antes de usarlos.
     */
    private void inicializarComponentesSeguros() {
        if (mainPanel != null) return;
        mainPanel = new JPanel();
        if (panelDatosComunes == null) panelDatosComunes = new JPanel();
        if (panelAsistente == null) panelAsistente = new JPanel();
        if (panelOrganizador == null) panelOrganizador = new JPanel();
        if (panelBotones == null) panelBotones = new JPanel();

        if (comboUsuarios == null) comboUsuarios = new JComboBox<>();
        if (comboTipoUsuario == null) comboTipoUsuario = new JComboBox<>();
        if (comboInstitucion == null) comboInstitucion = new JComboBox<>();

        if (txtNickname == null) txtNickname = new JTextField();
        if (txtNombre == null) txtNombre = new JTextField();
        if (txtCorreo == null) txtCorreo = new JTextField();
        if (txtApellido == null) txtApellido = new JTextField();
        if (txtFechaNacimiento == null) txtFechaNacimiento = new JTextField();
        if (txtDescripcion == null) txtDescripcion = new JTextArea();
        if (txtSitioWeb == null) txtSitioWeb = new JTextField();

        if (btnGuardar == null) btnGuardar = new JButton("Guardar");
        if (btnCancelar == null) btnCancelar = new JButton("Cancelar");
        txtContrasenia = new JPasswordField(24);
        txtConfirmacion = new JPasswordField(24);
        imagen = new ImagenSwing(true);
        txtDescripcion.setRows(3);
        panelDatosComunes = FormularioSwing.campos("Usuario:", comboUsuarios, "Nickname:", txtNickname, "Nombre:", txtNombre, "Correo:", txtCorreo, "Tipo:", comboTipoUsuario, "Nueva contraseña (opcional):", txtContrasenia, "Confirmar contraseña:", txtConfirmacion);
        panelAsistente = FormularioSwing.campos("Apellido:", txtApellido, "Nacimiento (AAAA-MM-DD):", txtFechaNacimiento, "Institución:", comboInstitucion);
        panelOrganizador = FormularioSwing.campos("Descripción:", txtDescripcion, "Sitio web:", txtSitioWeb);
        panelBotones = FormularioSwing.botones(btnGuardar, btnCancelar);
        mainPanel = FormularioSwing.componer(panelBotones, panelDatosComunes, panelAsistente, panelOrganizador, imagen);
        txtNickname.setEditable(false);
        txtCorreo.setEditable(false);
    }


    public void configurarTipoUsuario() {
        inicializarComponentesSeguros();

        comboTipoUsuario.removeAllItems();
        comboTipoUsuario.addItem("Asistente");
        comboTipoUsuario.addItem("Organizador");
        comboTipoUsuario.setEnabled(false);
    }
    public void cargarInstituciones() {
        inicializarComponentesSeguros();

        comboInstitucion.removeAllItems();
        comboInstitucion.addItem("Sin Institución");

        if (sistema != null && sistema.listarInstituciones() != null) {
            for (DtInstitucion institucion : sistema.listarInstituciones()) {
                comboInstitucion.addItem(institucion.getNombre());
            }
        }

        comboInstitucion.setSelectedIndex(0);
    }


    public void configurarEventos() {
        inicializarComponentesSeguros();

        if (comboTipoUsuario.getActionListeners().length == 0) {
            comboTipoUsuario.addActionListener(e -> actualizarCamposTipo());
        }

        if (comboUsuarios.getActionListeners().length == 0) {
            comboUsuarios.addActionListener(e -> seleccionarUsuario());
        }

        if (btnGuardar.getActionListeners().length == 0) {
            btnGuardar.addActionListener(e -> guardarCambios());
        }

        if (btnCancelar.getActionListeners().length == 0) {
            btnCancelar.addActionListener(e -> {
                limpiarFormulario();
                accionCerrar.run();
            });
        }
    }


    /**
     * Refresca usuarios e instituciones cada vez
     * que se abre el caso de uso.
     */
    public void refrescarUsuarios() {
        inicializarComponentesSeguros();
        configurarTipoUsuario();
        configurarEventos();
        cargarInstituciones();

        comboUsuarios.removeAllItems();

        if (sistema != null && sistema.listarUsuarios() != null) {
            for (DtUsuario usuario : sistema.listarUsuarios()) {
                comboUsuarios.addItem(usuario.getNickname());
            }
        }

        comboUsuarios.setSelectedIndex(-1);
        limpiarFormulario();
    }


    private void actualizarCamposTipo() {
        inicializarComponentesSeguros();

        String tipoSeleccionado = (String) comboTipoUsuario.getSelectedItem();
        boolean esAsistente = "Asistente".equals(tipoSeleccionado);

        panelAsistente.setVisible(esAsistente);
        panelOrganizador.setVisible(!esAsistente);

        mainPanel.revalidate();
        mainPanel.repaint();
    }


    private void seleccionarUsuario() {
        inicializarComponentesSeguros();

        String nickname = (String) comboUsuarios.getSelectedItem();
        if (nickname == null) { limpiarFormulario(); return; }

        DtUsuario usuario = sistema.consultarUsuario(nickname);
        if (usuario == null) return;

        cargarUsuario(usuario);
    }


    private void cargarUsuario(DtUsuario usuario) {
        usuarioSeleccionado = usuario;
        txtContrasenia.setText("");
        txtConfirmacion.setText("");
        imagen.cargar(sistema, TipoEntidadImagen.USUARIO, usuario.getNickname());
        cargarDatosComunes(
                usuario.getNickname(),
                usuario.getNombre(),
                usuario.getCorreoElectronico()
        );

        if (usuario instanceof DtAsistente asistente) {
            cargarAsistente(
                    asistente.getApellido(),
                    asistente.getFechaNacimiento() != null ? asistente.getFechaNacimiento().toString() : "",
                    asistente.getNombreInstitucion()
            );

        } else if (usuario instanceof DtOrganizador organizador) {
            cargarOrganizador(
                    organizador.getDescripcion(),
                    organizador.getSitioWeb()
            );
        }
    }


    private void cargarDatosComunes(String nickname, String nombre, String correo) {
        inicializarComponentesSeguros();

        txtNickname.setText(nickname);
        txtNombre.setText(nombre);
        txtCorreo.setText(correo);

        txtNickname.setEditable(false);
        txtCorreo.setEditable(false);
    }


    private void cargarAsistente(String apellido, String fechaNacimiento, String institucion) {
        inicializarComponentesSeguros();

        comboTipoUsuario.setSelectedItem("Asistente");

        txtApellido.setText(apellido);
        txtFechaNacimiento.setText(fechaNacimiento);

        if (institucion == null || institucion.isBlank()) {
            comboInstitucion.setSelectedItem("Sin Institución");
        } else {
            comboInstitucion.setSelectedItem(institucion);
        }

        actualizarCamposTipo();
    }


    private void cargarOrganizador(String descripcion, String sitioWeb) {
        inicializarComponentesSeguros();

        comboTipoUsuario.setSelectedItem("Organizador");

        txtDescripcion.setText(descripcion);
        txtSitioWeb.setText(sitioWeb);

        actualizarCamposTipo();
    }


    private void guardarCambios() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(mainPanel, "Seleccione un usuario.", "Modificar usuario", JOptionPane.WARNING_MESSAGE);
            return;
        }
        char[] clave = txtContrasenia.getPassword();
        char[] confirmacion = txtConfirmacion.getPassword();
        try {
            if (!Arrays.equals(clave, confirmacion)) throw new IllegalArgumentException("La contraseña y su confirmación no coinciden.");
            DtImagen nuevaImagen = imagen.leerValidada(sistema);
            String nickname = usuarioSeleccionado.getNickname();
            String correo = usuarioSeleccionado.getCorreoElectronico();
            if (usuarioSeleccionado instanceof DtAsistente) {
                guardarAsistente(nickname, txtNombre.getText().trim(), correo, new String(clave), new String(confirmacion));
            } else {
                guardarOrganizador(nickname, txtNombre.getText().trim(), correo, new String(clave), new String(confirmacion));
            }
            // No se repite el cambio de contraseña si la asociación posterior falla.
            txtContrasenia.setText("");
            txtConfirmacion.setText("");
            usuarioSeleccionado = sistema.consultarUsuario(nickname);
            String errorImagen = ImagenSwing.asociar(sistema, TipoEntidadImagen.USUARIO, nickname, nuevaImagen);
            if (errorImagen == null) {
                imagen.cargar(sistema, TipoEntidadImagen.USUARIO, nickname);
                mostrarExito();
            } else {
                JOptionPane.showMessageDialog(mainPanel, errorImagen + " Puede reintentar guardar para asociarla.", "Modificación parcial", JOptionPane.WARNING_MESSAGE);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(mainPanel, "La fecha debe tener formato AAAA-MM-DD.", "Modificar usuario", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException | IOException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(), "Modificar usuario", JOptionPane.WARNING_MESSAGE);
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(confirmacion, '\0');
        }
    }

    private void guardarAsistente(String nickname, String nombre, String correo, String clave, String confirmacion) {
        String institucion = (String) comboInstitucion.getSelectedItem();
        if ("Sin Institución".equals(institucion)) institucion = null;
        DtAsistente dt = new DtAsistente(nickname, nombre, correo, txtApellido.getText().trim(),
                LocalDate.parse(txtFechaNacimiento.getText().trim()), institucion);
        dt.setContrasenia(clave);
        dt.setConfirmacionContrasenia(confirmacion);
        sistema.modificarAsistente(dt);
    }

    private void guardarOrganizador(String nickname, String nombre, String correo, String clave, String confirmacion) {
        DtOrganizador dt = new DtOrganizador(nickname, nombre, correo, txtDescripcion.getText().trim(), txtSitioWeb.getText().trim());
        dt.setContrasenia(clave);
        dt.setConfirmacionContrasenia(confirmacion);
        sistema.modificarOrganizador(dt);
    }

    private void mostrarExito() {
        JOptionPane.showMessageDialog(
                mainPanel,
                "Usuario modificado correctamente.",
                "Modificar usuario",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private void limpiarFormulario() {
        inicializarComponentesSeguros();
        usuarioSeleccionado = null;
        txtContrasenia.setText("");
        txtConfirmacion.setText("");
        imagen.mostrar(null);

        txtNickname.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");

        txtApellido.setText("");
        txtFechaNacimiento.setText("");

        txtDescripcion.setText("");
        txtSitioWeb.setText("");

        if (comboInstitucion.getItemCount() > 0) {
            comboInstitucion.setSelectedIndex(0);
        }
    }


    public JPanel getMainPanel() {
        inicializarComponentesSeguros();
        return mainPanel;
    }


    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
}