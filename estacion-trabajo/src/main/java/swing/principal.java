package swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;

public class principal {

    private JPanel panelPrincipal;
    private JDesktopPane desktopPane;
    private JPanel panelInicio;

    private JInternalFrame internoAltaUsuario;
    private JInternalFrame internoModificarUsuario;
    private JInternalFrame internoConsultaUsuario;
    private JInternalFrame internoAltaInstitucion;
    private JInternalFrame internoAltaEvento;
    private JInternalFrame internoConsultaEvento;
    private JInternalFrame internoAltaPatrocinio;
    private JInternalFrame internoConsultaPatrocinio;
    private JInternalFrame internoAltaEdicion;
    private JInternalFrame internoConsultaEdicion;
    private JInternalFrame internoAltaCategoria;
    private JInternalFrame internoAltaTipoRegistro;
    private JInternalFrame internoConsultaTipoRegistro;
    private JInternalFrame internoRegistroEdicion;
    private JInternalFrame internoConsultaRegistro;

    private AltaUsuarioPanel panelAltaUsuario;
    private ModificarUsuarioPanel panelModificarUsuario;
    private ConsultaUsuarioPanel panelConsultaUsuario;
    private AltaEventoPanel panelAltaEvento;
    private ConsultaEventoPanel panelConsultaEvento;
    private AltaPatrocinioPanel panelAltaPatrocinio;
    private ConsultaPatrocinioPanel panelConsultaPatrocinio;
    private AltaEdicionEventoPanel panelAltaEdicion;
    private ConsultaEdicionEventoPanel panelConsultaEdicion;
    private AltaCategoriaPanel panelAltaCategoria;
    private AltaTipoRegistro panelAltaTipoRegistro;
    private ConsultaTipoRegistroPanel panelConsultaTipoRegistro;
    private RegistroEdicionEventoPanel panelRegistroEdicion;
    private ConsultaRegistroPanel panelConsultaRegistro;

    public principal() {
        panelPrincipal = new JPanel(new BorderLayout());
        desktopPane = new JDesktopPane();

        panelPrincipal.add(desktopPane, BorderLayout.CENTER);

        inicializarPanelInicio();
        inicializarVentanasInternas();
    }

    private void inicializarPanelInicio() {
        panelInicio = crearPanelInicio();

        desktopPane.add(panelInicio, JLayeredPane.DEFAULT_LAYER);

        desktopPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarPanelInicio();
            }
        });
    }

    private void ajustarPanelInicio() {
        panelInicio.setBounds(0, 0, desktopPane.getWidth(), desktopPane.getHeight());
    }

    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(245, 246, 248));
        panel.setBorder(BorderFactory.createEmptyBorder(45, 80, 35, 80));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("eventos.uy");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 34));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Sistema de gestión de eventos");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(90, 90, 90));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(subtitulo);

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 20, 20));
        tarjetas.setOpaque(false);
        tarjetas.setBorder(BorderFactory.createEmptyBorder(55, 120, 55, 120));

        tarjetas.add(crearTarjeta(
                "Usuarios",
                "Alta, consulta y modificación de usuarios"
        ));

        tarjetas.add(crearTarjeta(
                "Eventos",
                "Eventos, ediciones, categorías y tipos de registro"
        ));

        tarjetas.add(crearTarjeta(
                "Registros",
                "Registro de asistentes y consulta de inscripciones"
        ));

        tarjetas.add(crearTarjeta(
                "Patrocinios",
                "Alta y consulta de patrocinios"
        ));

        JLabel pie = new JLabel(
                "Programación de Aplicaciones - 2026",
                SwingConstants.CENTER
        );
        pie.setForeground(new Color(110, 110, 110));

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(tarjetas, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearTarjeta(String titulo, String descripcion) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));
        tarjeta.setBackground(Color.WHITE);

        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(215, 215, 215)),
                        BorderFactory.createEmptyBorder(25, 25, 25, 25)
                )
        );

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel lblDescripcion = new JLabel(
                "<html><div style='text-align:center;'>"
                        + descripcion
                        + "</div></html>",
                SwingConstants.CENTER
        );
        lblDescripcion.setForeground(new Color(90, 90, 90));

        tarjeta.add(lblTitulo, BorderLayout.CENTER);
        tarjeta.add(lblDescripcion, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void inicializarVentanasInternas() {
        panelAltaUsuario = new AltaUsuarioPanel();
        internoAltaUsuario = crearInterno(
                "Alta Usuario",
                panelAltaUsuario.getMainPanel(),
                20,
                20
        );
        panelAltaUsuario.setAccionCerrar(
                () -> internoAltaUsuario.setVisible(false)
        );
        agregarInterno(internoAltaUsuario);

        panelModificarUsuario = new ModificarUsuarioPanel();
        internoModificarUsuario = crearInterno(
                "Modificar Datos de Usuario",
                panelModificarUsuario.getMainPanel(),
                60,
                60
        );
        panelModificarUsuario.setAccionCerrar(
                () -> internoModificarUsuario.setVisible(false)
        );
        agregarInterno(internoModificarUsuario);

        panelConsultaUsuario = new ConsultaUsuarioPanel(this);
        internoConsultaUsuario = crearInterno(
                "Consulta Usuario",
                panelConsultaUsuario.getMainPanel(),
                80,
                80
        );
        panelConsultaUsuario.setAccionCerrar(
                () -> internoConsultaUsuario.setVisible(false)
        );
        agregarInterno(internoConsultaUsuario);

        AltaInstitucionPanel panelAltaInstitucion = new AltaInstitucionPanel();
        internoAltaInstitucion = crearInterno(
                "Alta Institución",
                panelAltaInstitucion.getMainPanel(),
                60,
                60
        );
        panelAltaInstitucion.setAccionCerrar(
                () -> internoAltaInstitucion.setVisible(false)
        );
        internoAltaInstitucion.setSize(600, 420);
        internoAltaInstitucion.setMinimumSize(new Dimension(500, 350));
        agregarInterno(internoAltaInstitucion);

        panelAltaEvento = new AltaEventoPanel();
        internoAltaEvento = crearInterno(
                "Alta Evento",
                panelAltaEvento.getMainPanel(),
                80,
                80
        );
        panelAltaEvento.setAccionCerrar(
                () -> internoAltaEvento.setVisible(false)
        );
        agregarInterno(internoAltaEvento);

        panelConsultaEvento = new ConsultaEventoPanel(this);
        internoConsultaEvento = crearInterno(
                "Consulta Evento",
                panelConsultaEvento,
                80,
                80
        );
        agregarInterno(internoConsultaEvento);

        panelAltaPatrocinio = new AltaPatrocinioPanel();
        internoAltaPatrocinio = crearInterno(
                "Alta Patrocinio",
                panelAltaPatrocinio.getMainPanel(),
                60,
                60
        );
        panelAltaPatrocinio.setAccionCerrar(
                () -> internoAltaPatrocinio.setVisible(false)
        );
        agregarInterno(internoAltaPatrocinio);

        panelConsultaPatrocinio = new ConsultaPatrocinioPanel();
        internoConsultaPatrocinio = crearInterno(
                "Consulta Patrocinio",
                panelConsultaPatrocinio.getMainPanel(),
                60,
                60
        );
        panelConsultaPatrocinio.setAccionCerrar(
                () -> internoConsultaPatrocinio.setVisible(false)
        );
        agregarInterno(internoConsultaPatrocinio);

        panelAltaEdicion = new AltaEdicionEventoPanel();
        internoAltaEdicion = crearInterno(
                "Alta Edición de Evento",
                panelAltaEdicion,
                70,
                70
        );
        agregarInterno(internoAltaEdicion);

        panelConsultaEdicion = new ConsultaEdicionEventoPanel();
        internoConsultaEdicion = crearInterno(
                "Consulta Edición de Evento",
                panelConsultaEdicion,
                110,
                110
        );
        agregarInterno(internoConsultaEdicion);

        panelAltaCategoria = new AltaCategoriaPanel();
        internoAltaCategoria = crearInterno(
                "Alta Categoría",
                panelAltaCategoria.getMainPanel(),
                80,
                80
        );
        panelAltaCategoria.setAccionCerrar(
                () -> internoAltaCategoria.setVisible(false)
        );
        agregarInterno(internoAltaCategoria);

        panelAltaTipoRegistro = new AltaTipoRegistro();
        internoAltaTipoRegistro = crearInterno(
                "Alta Tipo de Registro",
                panelAltaTipoRegistro.getMainPanel(),
                90,
                90
        );
        internoAltaTipoRegistro.setSize(650, 450);
        internoAltaTipoRegistro.setMinimumSize(new Dimension(520, 360));
        panelAltaTipoRegistro.setAccionCerrar(
                () -> internoAltaTipoRegistro.setVisible(false)
        );
        agregarInterno(internoAltaTipoRegistro);

        panelConsultaTipoRegistro = new ConsultaTipoRegistroPanel();
        internoConsultaTipoRegistro = crearInterno(
                "Consulta Tipo de Registro",
                panelConsultaTipoRegistro,
                90,
                90
        );
        agregarInterno(internoConsultaTipoRegistro);

        panelRegistroEdicion = new RegistroEdicionEventoPanel();
        internoRegistroEdicion = crearInterno(
                "Registro a Edición de Evento",
                panelRegistroEdicion,
                100,
                100
        );
        agregarInterno(internoRegistroEdicion);

        panelConsultaRegistro = new ConsultaRegistroPanel();
        internoConsultaRegistro = crearInterno(
                "Consulta de Registro de Edición de Evento",
                panelConsultaRegistro,
                100,
                100
        );
        agregarInterno(internoConsultaRegistro);
    }

    private void agregarInterno(JInternalFrame interno) {
        desktopPane.add(interno, JLayeredPane.PALETTE_LAYER);
    }

    private JMenuBar crearMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuUsuarios = new JMenu("Usuarios");
        JMenu menuEventos = new JMenu("Eventos");
        JMenu menuRegistro = new JMenu("Registros");
        JMenu menuPatrocinio = new JMenu("Patrocinios");
        JMenu menuInstitucion = new JMenu("Instituciones");
        JMenu menuSesion = new JMenu("Sesión");

        JMenuItem altaUsuario = new JMenuItem("Alta Usuario");
        JMenuItem modificarUsuario = new JMenuItem("Modificar Datos de Usuario");
        JMenuItem consultaUsuario = new JMenuItem("Consulta Usuario");

        JMenuItem altaInstitucion = new JMenuItem("Alta Institución");

        JMenuItem altaEvento = new JMenuItem("Alta Evento");
        JMenuItem consultaEvento = new JMenuItem("Consulta Evento");
        JMenuItem altaEdicion = new JMenuItem("Alta Edición");
        JMenuItem consultaEdicion = new JMenuItem("Consulta Edición");
        JMenuItem altaTipoRegistro = new JMenuItem("Alta Tipo de Registro");
        JMenuItem consultaTipoRegistro = new JMenuItem("Consulta Tipo de Registro");
        JMenuItem altaCategoria = new JMenuItem("Alta Categoría");

        JMenuItem registroEdicionEvento = new JMenuItem(
                "Registro a Edición de Evento"
        );
        JMenuItem consultaRegistroEvento = new JMenuItem(
                "Consulta de Registro de Edición de Evento"
        );

        JMenuItem altaPatrocinio = new JMenuItem("Alta Patrocinio");
        JMenuItem consultaPatrocinio = new JMenuItem("Consulta Patrocinio");

        JMenuItem itemSalir = new JMenuItem("Salir");

        altaUsuario.addActionListener(e -> {
            panelAltaUsuario.refrescarDatos();
            mostrar(internoAltaUsuario);
        });

        modificarUsuario.addActionListener(e -> {
            panelModificarUsuario.cargarInstituciones();
            panelModificarUsuario.refrescarUsuarios();
            mostrar(internoModificarUsuario);
        });

        consultaUsuario.addActionListener(e -> {
            panelConsultaUsuario.recargarUsuarios();
            mostrar(internoConsultaUsuario);
        });

        altaInstitucion.addActionListener(e ->
                mostrar(internoAltaInstitucion)
        );

        altaEvento.addActionListener(e -> {
            panelAltaEvento.cargarCategorias();
            mostrar(internoAltaEvento);
        });

        consultaEvento.addActionListener(e -> {
            panelConsultaEvento.cargarComboEventos();
            mostrar(internoConsultaEvento);
        });

        altaEdicion.addActionListener(e -> {
            panelAltaEdicion.cargarEventos();
            panelAltaEdicion.cargarOrganizadores();
            mostrar(internoAltaEdicion);
        });

        consultaEdicion.addActionListener(e -> {
            panelConsultaEdicion.cargarEventos();
            mostrar(internoConsultaEdicion);
        });

        altaTipoRegistro.addActionListener(e -> {
            panelAltaTipoRegistro.refrescarDatos();
            mostrar(internoAltaTipoRegistro);
        });

        consultaTipoRegistro.addActionListener(e -> {
            panelConsultaTipoRegistro.cargarEventos();
            mostrar(internoConsultaTipoRegistro);
        });

        altaCategoria.addActionListener(e -> {
            panelAltaCategoria.cargarArbolCategorias();
            mostrar(internoAltaCategoria);
        });

        registroEdicionEvento.addActionListener(e -> {
            panelRegistroEdicion.cargarAsistentes();
            panelRegistroEdicion.cargarEventos();
            mostrar(internoRegistroEdicion);
        });

        consultaRegistroEvento.addActionListener(e -> {
            panelConsultaRegistro.refrescarDatos();
            mostrar(internoConsultaRegistro);
        });

        altaPatrocinio.addActionListener(e -> {
            panelAltaPatrocinio.refrescarDatos();
            mostrar(internoAltaPatrocinio);
        });

        consultaPatrocinio.addActionListener(e -> {
            panelConsultaPatrocinio.refrescarDatos();
            mostrar(internoConsultaPatrocinio);
        });

        itemSalir.addActionListener(e -> System.exit(0));

        menuUsuarios.add(altaUsuario);
        menuUsuarios.add(modificarUsuario);
        menuUsuarios.add(consultaUsuario);

        menuEventos.add(altaEvento);
        menuEventos.add(consultaEvento);
        menuEventos.addSeparator();
        menuEventos.add(altaEdicion);
        menuEventos.add(consultaEdicion);
        menuEventos.addSeparator();
        menuEventos.add(altaTipoRegistro);
        menuEventos.add(consultaTipoRegistro);
        menuEventos.addSeparator();
        menuEventos.add(altaCategoria);

        menuRegistro.add(registroEdicionEvento);
        menuRegistro.add(consultaRegistroEvento);

        menuPatrocinio.add(altaPatrocinio);
        menuPatrocinio.add(consultaPatrocinio);

        menuInstitucion.add(altaInstitucion);

        menuSesion.add(itemSalir);

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuRegistro);
        menuBar.add(menuPatrocinio);
        menuBar.add(menuInstitucion);
        menuBar.add(menuSesion);

        return menuBar;
    }

    private void mostrar(JInternalFrame interno) {
        int x = Math.max(
                0,
                (desktopPane.getWidth() - interno.getWidth()) / 2
        );

        int y = Math.max(
                0,
                (desktopPane.getHeight() - interno.getHeight()) / 2
        );

        interno.setLocation(x, y);
        interno.setVisible(true);
        interno.toFront();

        try {
            interno.setSelected(true);
        } catch (PropertyVetoException ignored) {
        }
    }

    private JInternalFrame crearInterno(
            String titulo,
            Container contenido,
            int x,
            int y
    ) {
        JInternalFrame interno = new JInternalFrame(
                titulo,
                true,
                true,
                true,
                true
        );

        interno.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        interno.setFrameIcon(null);
        interno.setContentPane(contenido);
        interno.pack();

        Dimension actual = interno.getSize();

        int ancho = Math.max(
                400,
                Math.min(actual.width, 850)
        );

        int alto = Math.max(
                300,
                Math.min(actual.height, 650)
        );

        interno.setSize(ancho, alto);
        interno.setMinimumSize(new Dimension(380, 280));
        interno.setLocation(x, y);
        interno.setVisible(false);

        return interno;
    }

    public void navegarAConsultaEdicion(
            String nombreEvento,
            String nombreEdicion
    ) {
        panelConsultaEdicion.seleccionarEventoYEdicion(
                nombreEvento,
                nombreEdicion
        );

        mostrar(internoConsultaEdicion);
    }

    public void navegarAConsultaRegistro(
            String nicknameAsistente,
            String nombreEdicion
    ) {
        panelConsultaRegistro.seleccionarRegistro(
                nicknameAsistente,
                nombreEdicion
        );

        mostrar(internoConsultaRegistro);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            principal ventana = new principal();

            JFrame frame = new JFrame("eventos.uy");
            frame.setContentPane(ventana.panelPrincipal);
            frame.setJMenuBar(ventana.crearMenu());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}