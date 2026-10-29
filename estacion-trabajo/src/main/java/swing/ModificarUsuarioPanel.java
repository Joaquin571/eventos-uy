package swing;

import datatypes.DtAsistente;
import datatypes.DtImagen;
import datatypes.DtInstitucion;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import datatypes.TipoEntidadImagen;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    // Contraseña e imagen (opcionales: si se dejan vacías no se modifican)
    private JPasswordField txtContrasenia;
    private JPasswordField txtConfirmacion;
    private JButton btnImagen;
    private transient DtImagen imagenElegida;

    private static final String TEXTO_BOTON_IMAGEN = "Elegir imagen (opcional)...";

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
        sistema = Fabrica.getInstance().getISistema();
    }

    /**
     * Asegura que los componentes gráficos no sean null antes de usarlos.
     */
    private void inicializarComponentesSeguros() {
        if (mainPanel == null) mainPanel = new JPanel();
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
        if (txtContrasenia == null) txtContrasenia = new JPasswordField();
        if (txtConfirmacion == null) txtConfirmacion = new JPasswordField();
        if (btnImagen == null) btnImagen = new JButton(TEXTO_BOTON_IMAGEN);

        if (btnGuardar == null) btnGuardar = new JButton("Guardar");
        if (btnCancelar == null) btnCancelar = new JButton("Cancelar");
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

        if (btnImagen.getActionListeners().length == 0) {
            btnImagen.addActionListener(e -> elegirImagen());
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
        if (nickname == null) return;

        DtUsuario usuario = sistema.consultarUsuario(nickname);
        if (usuario == null) return;

        cargarUsuario(usuario);
    }


    private void cargarUsuario(DtUsuario usuario) {
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
        inicializarComponentesSeguros();

        String nickname = txtNickname.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String tipo = (String) comboTipoUsuario.getSelectedItem();

        if (nickname.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Complete los datos obligatorios.",
                    "Modificar usuario",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // La contraseña es opcional: si se escribe algo, tiene que coincidir con la confirmación
        String contrasenia = new String(txtContrasenia.getPassword());
        String confirmacion = new String(txtConfirmacion.getPassword());

        if (!contrasenia.isEmpty() || !confirmacion.isEmpty()) {
            if (!contrasenia.equals(confirmacion)) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "La contraseña y su confirmación no coinciden.",
                        "Modificar usuario",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }

        if ("Asistente".equals(tipo)) {
            guardarAsistente(nickname, nombre, correo);
        } else if ("Organizador".equals(tipo)) {
            guardarOrganizador(nickname, nombre, correo);
        }
    }


    private void guardarAsistente(String nickname, String nombre, String correo) {
        inicializarComponentesSeguros();

        String apellido = txtApellido.getText().trim();
        String fechaTexto = txtFechaNacimiento.getText().trim();
        String institucion = (String) comboInstitucion.getSelectedItem();

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
            LocalDate fechaNacimiento = LocalDate.parse(fechaTexto);

            if ("Sin Institución".equals(institucion)) {
                institucion = null;
            }

            DtAsistente dt = new DtAsistente(
                    nickname,
                    nombre,
                    correo,
                    apellido,
                    fechaNacimiento,
                    institucion
            );

            aplicarContrasenia(dt);
            sistema.modificarAsistente(dt);
            guardarImagen(nickname);
            mostrarExito();

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "La fecha debe tener formato AAAA-MM-DD.",
                    "Modificar usuario",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }


    private void guardarOrganizador(String nickname, String nombre, String correo) {
        inicializarComponentesSeguros();

        String descripcion = txtDescripcion.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();

        DtOrganizador dt = new DtOrganizador(
                nickname,
                nombre,
                correo,
                descripcion,
                sitioWeb
        );

        try {
            aplicarContrasenia(dt);
            sistema.modificarOrganizador(dt);
            guardarImagen(nickname);
            mostrarExito();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }


    // Si el administrador escribió una contraseña nueva, viaja en el Dt (la lógica la valida y la guarda)
    private void aplicarContrasenia(DtUsuario dt) {
        String contrasenia = new String(txtContrasenia.getPassword());

        if (!contrasenia.isEmpty()) {
            dt.setContrasenia(contrasenia);
            dt.setConfirmacionContrasenia(new String(txtConfirmacion.getPassword()));
        }
    }


    // Si eligió una imagen, se asocia al usuario a través de la lógica
    private void guardarImagen(String nickname) {
        if (imagenElegida != null) {
            sistema.asociarImagen(TipoEntidadImagen.USUARIO, nickname, imagenElegida);
        }
    }


    private void elegirImagen() {
        inicializarComponentesSeguros();

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Elegir imagen");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg, gif, webp)", "png", "jpg", "jpeg", "gif", "webp"));

        if (selector.showOpenDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        String tipo = tipoContenido(archivo.getName());

        if (tipo == null) {
            mostrarError("Elegí una imagen PNG, JPG, GIF o WEBP.");
            return;
        }

        try {
            DtImagen imagen = new DtImagen(tipo, Files.readAllBytes(archivo.toPath()));
            sistema.validarImagen(imagen);
            imagenElegida = imagen;
            btnImagen.setText(archivo.getName());
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (IOException e) {
            mostrarError("No se pudo leer el archivo.");
        }
    }


    private String tipoContenido(String nombreArchivo) {
        String nombre = nombreArchivo.toLowerCase();

        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return "image/jpeg";
        if (nombre.endsWith(".gif")) return "image/gif";
        if (nombre.endsWith(".webp")) return "image/webp";
        return null;
    }


    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                mainPanel,
                mensaje,
                "Modificar usuario",
                JOptionPane.WARNING_MESSAGE
        );
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

        txtNickname.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");

        txtApellido.setText("");
        txtFechaNacimiento.setText("");

        txtDescripcion.setText("");
        txtSitioWeb.setText("");

        txtContrasenia.setText("");
        txtConfirmacion.setText("");
        imagenElegida = null;
        btnImagen.setText(TEXTO_BOTON_IMAGEN);

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