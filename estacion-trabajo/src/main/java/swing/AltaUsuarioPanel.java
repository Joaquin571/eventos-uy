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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AltaUsuarioPanel {

    private static final String SIN_INSTITUCION = "Sin Institución";
    private static final long TAMANIO_MAXIMO_IMAGEN = 2L * 1024 * 1024;

    private final transient ISistema sistema;

    private JPanel mainPanel;

    private JRadioButton radioAsistente;
    private JRadioButton radioOrganizador;

    // Comunes
    private JTextField txtNickname;
    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtContrasenia;
    private JPasswordField txtConfirmacion;

    // Imagen opcional
    private JLabel lblImagen;
    private JButton btnElegirImagen;
    private JButton btnQuitarImagen;
    private File imagenElegida;

    // Asistente
    private JTextField txtApellido;
    private JTextField txtFechaNac;
    private JComboBox<String> comboInstitucion;

    // Organizador
    private JTextArea txtDescripcion;
    private JTextField txtSitioWeb;

    private JButton btnAceptar;
    private JButton btnCancelar;

    private JPanel panelAsistente;
    private JPanel panelOrganizador;

    private Runnable accionCerrar = () -> {};

    public AltaUsuarioPanel() {
        sistema = Fabrica.getInstance().getISistema();

        inicializarComponentes();
        cargarInstituciones();

        radioAsistente.addActionListener(e -> cambiarTipoUsuario(true));
        radioOrganizador.addActionListener(e -> cambiarTipoUsuario(false));
        btnElegirImagen.addActionListener(e -> elegirImagen());
        btnQuitarImagen.addActionListener(e -> quitarImagen());
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

    public void refrescarDatos() {
        cargarInstituciones();
    }

    public void cargarInstituciones() {
        comboInstitucion.removeAllItems();
        comboInstitucion.addItem(SIN_INSTITUCION);

        for (DtInstitucion institucion : sistema.listarInstituciones()) {
            comboInstitucion.addItem(institucion.getNombre());
        }

        comboInstitucion.setSelectedIndex(0);
    }

    private void cambiarTipoUsuario(boolean esAsistente) {
        panelAsistente.setVisible(esAsistente);
        panelOrganizador.setVisible(!esAsistente);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // ---------------------------------------------------------------- imagen

    private void elegirImagen() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg, gif, webp)", "png", "jpg", "jpeg", "gif", "webp"));

        if (selector.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
            imagenElegida = selector.getSelectedFile();
            lblImagen.setText(imagenElegida.getName());
        }
    }

    private void quitarImagen() {
        imagenElegida = null;
        lblImagen.setText("Ninguna");
    }

    private String tipoContenido(File archivo) {
        String nombre = archivo.getName().toLowerCase();

        if (nombre.endsWith(".png")) return "image/png";
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) return "image/jpeg";
        if (nombre.endsWith(".gif")) return "image/gif";
        if (nombre.endsWith(".webp")) return "image/webp";

        // La lógica rechaza cualquier formato que no sea de imagen
        return "application/octet-stream";
    }

    private DtImagen leerImagen() throws IOException {
        if (imagenElegida == null) {
            return null;
        }

        if (imagenElegida.length() > TAMANIO_MAXIMO_IMAGEN) {
            throw new IllegalArgumentException("La imagen supera el tamaño máximo de 2 MB.");
        }

        return new DtImagen(tipoContenido(imagenElegida), Files.readAllBytes(imagenElegida.toPath()));
    }

    // ------------------------------------------------------------------ alta

    private void registrarUsuario() {
        try {
            String nickname = txtNickname.getText().trim();
            String nombre = txtNombre.getText().trim();
            String correo = txtCorreo.getText().trim();
            String contrasenia = new String(txtContrasenia.getPassword());
            String confirmacion = new String(txtConfirmacion.getPassword());

            if (nickname.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
                throw new IllegalArgumentException(
                        "Complete los campos obligatorios: Nickname, Nombre y Correo.");
            }

            if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Ingrese un correo electrónico válido.");
            }

            if (sistema.existeUsuario(nickname)) {
                throw new IllegalArgumentException(
                        "El nickname '" + nickname + "' ya existe en el sistema.");
            }

            if (sistema.existeCorreoElectronico(correo)) {
                throw new IllegalArgumentException(
                        "El correo electrónico '" + correo + "' ya se encuentra registrado.");
            }

            if (contrasenia.isEmpty()) {
                throw new IllegalArgumentException("La contraseña es obligatoria.");
            }

            if (!contrasenia.equals(confirmacion)) {
                throw new IllegalArgumentException("La contraseña y su confirmación no coinciden.");
            }

            // La imagen se valida antes del alta, para no dejar el usuario a medias
            DtImagen imagen = leerImagen();

            if (imagen != null) {
                sistema.validarImagen(imagen);
            }

            boolean esAsistente = radioAsistente.isSelected();

            DtUsuario dt = esAsistente
                    ? armarAsistente(nickname, nombre, correo)
                    : armarOrganizador(nickname, nombre, correo);

            dt.setContrasenia(contrasenia);
            dt.setConfirmacionContrasenia(confirmacion);

            boolean alta = esAsistente
                    ? sistema.altaAsistente((DtAsistente) dt)
                    : sistema.altaOrganizador((DtOrganizador) dt);

            if (!alta) {
                throw new IllegalArgumentException("No se pudo registrar el usuario.");
            }

            if (imagen != null) {
                sistema.asociarImagen(TipoEntidadImagen.USUARIO, nickname, imagen);
            }

            JOptionPane.showMessageDialog(mainPanel, "Usuario registrado con éxito.",
                    "Alta Usuario", JOptionPane.INFORMATION_MESSAGE);

            limpiar();
            accionCerrar.run();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainPanel, ex.getMessage(),
                    "Alta Usuario", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainPanel, "No se pudo leer la imagen: " + ex.getMessage(),
                    "Alta Usuario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DtAsistente armarAsistente(String nickname, String nombre, String correo) {
        String apellido = txtApellido.getText().trim();
        String fechaTexto = txtFechaNac.getText().trim();

        if (apellido.isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio para asistentes.");
        }

        if (fechaTexto.isEmpty()) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }

        LocalDate fechaNac;

        try {
            fechaNac = LocalDate.parse(fechaTexto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Debe usar AAAA-MM-DD.");
        }

        if (!fechaNac.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento debe ser anterior a la fecha actual.");
        }

        String institucion = (String) comboInstitucion.getSelectedItem();

        if (SIN_INSTITUCION.equals(institucion)) {
            institucion = null;
        }

        return new DtAsistente(nickname, nombre, correo, apellido, fechaNac, institucion);
    }

    private DtOrganizador armarOrganizador(String nickname, String nombre, String correo) {
        String descripcion = txtDescripcion.getText().trim();
        String sitioWeb = txtSitioWeb.getText().trim();

        if (descripcion.isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria para organizadores.");
        }

        return new DtOrganizador(nickname, nombre, correo, descripcion,
                sitioWeb.isEmpty() ? null : sitioWeb);
    }

    private void limpiar() {
        txtNickname.setText("");
        txtNombre.setText("");
        txtCorreo.setText("");
        txtContrasenia.setText("");
        txtConfirmacion.setText("");
        txtApellido.setText("");
        txtFechaNac.setText("");
        txtDescripcion.setText("");
        txtSitioWeb.setText("");
        quitarImagen();

        radioAsistente.setSelected(true);
        cambiarTipoUsuario(true);

        if (comboInstitucion.getItemCount() > 0) {
            comboInstitucion.setSelectedIndex(0);
        }
    }

    // ------------------------------------------------------------ componentes

    private void agregarFila(JPanel panel, String etiqueta, JComponent campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void inicializarComponentes() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tipo de usuario
        JPanel panelTipo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTipo.setBorder(BorderFactory.createTitledBorder("Tipo de Usuario"));

        radioAsistente = new JRadioButton("Asistente", true);
        radioOrganizador = new JRadioButton("Organizador");

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(radioAsistente);
        grupo.add(radioOrganizador);

        panelTipo.add(radioAsistente);
        panelTipo.add(radioOrganizador);

        // Datos generales y de acceso
        JPanel panelComun = new JPanel(new GridLayout(5, 2, 5, 5));
        panelComun.setBorder(BorderFactory.createTitledBorder("Datos Generales"));

        txtNickname = new JTextField();
        txtNombre = new JTextField();
        txtCorreo = new JTextField();
        txtContrasenia = new JPasswordField();
        txtConfirmacion = new JPasswordField();

        agregarFila(panelComun, "Nickname (*):", txtNickname);
        agregarFila(panelComun, "Nombre (*):", txtNombre);
        agregarFila(panelComun, "Correo (*):", txtCorreo);
        agregarFila(panelComun, "Contraseña (*):", txtContrasenia);
        agregarFila(panelComun, "Confirmar contraseña (*):", txtConfirmacion);

        // Imagen opcional
        JPanel panelImagen = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelImagen.setBorder(BorderFactory.createTitledBorder("Imagen (opcional, hasta 2 MB)"));

        lblImagen = new JLabel("Ninguna");
        btnElegirImagen = new JButton("Elegir...");
        btnQuitarImagen = new JButton("Quitar");

        panelImagen.add(btnElegirImagen);
        panelImagen.add(btnQuitarImagen);
        panelImagen.add(lblImagen);

        // Asistente
        panelAsistente = new JPanel(new GridLayout(3, 2, 5, 5));
        panelAsistente.setBorder(BorderFactory.createTitledBorder("Detalles del Asistente"));

        txtApellido = new JTextField();
        txtFechaNac = new JTextField();
        txtFechaNac.setToolTipText("Ejemplo: 1999-09-09");
        comboInstitucion = new JComboBox<>();

        agregarFila(panelAsistente, "Apellido (*):", txtApellido);
        agregarFila(panelAsistente, "Fecha Nacimiento (AAAA-MM-DD) (*):", txtFechaNac);
        agregarFila(panelAsistente, "Institución:", comboInstitucion);

        // Organizador
        panelOrganizador = new JPanel(new BorderLayout(5, 5));
        panelOrganizador.setBorder(BorderFactory.createTitledBorder("Detalles del Organizador"));

        txtDescripcion = new JTextArea(4, 25);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setPreferredSize(new Dimension(220, 80));

        JPanel filaDescripcion = new JPanel(new BorderLayout(5, 5));
        filaDescripcion.add(new JLabel("Descripción (*):"), BorderLayout.NORTH);
        filaDescripcion.add(scrollDescripcion, BorderLayout.CENTER);

        txtSitioWeb = new JTextField();
        JPanel filaSitio = new JPanel(new GridLayout(1, 2, 5, 5));
        agregarFila(filaSitio, "Sitio Web:", txtSitioWeb);

        panelOrganizador.add(filaDescripcion, BorderLayout.CENTER);
        panelOrganizador.add(filaSitio, BorderLayout.SOUTH);

        // Formulario
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.add(panelComun);
        panelForm.add(Box.createVerticalStrut(10));
        panelForm.add(panelImagen);
        panelForm.add(Box.createVerticalStrut(10));
        panelForm.add(panelAsistente);
        panelForm.add(panelOrganizador);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar = new JButton("Cancelar");
        btnAceptar = new JButton("Aceptar");
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);

        mainPanel.add(panelTipo, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(panelForm), BorderLayout.CENTER);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);
    }
}