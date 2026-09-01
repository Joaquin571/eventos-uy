package swing;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

public class principal {

    private JPanel panelPrincipal;
    private JDesktopPane desktopPane;

    // INTERNAL FRAMES
    private JInternalFrame internoAltaUsuario;
    private JInternalFrame internoModificarUsuario;
    private JInternalFrame internoConsultaUsuario;
    private JInternalFrame internoAltaInstitucion;
    private JInternalFrame internoAltaEvento;
    private AltaEventoPanel panelAltaEvento;
    private JInternalFrame internoConsultaEvento;
    private ConsultaEventoPanel panelConsultaEvento;
    private JInternalFrame internoAltaPatrocinio;
    private JInternalFrame internoConsultaPatrocinio;
    private JInternalFrame internoAltaEdicion;
    private JInternalFrame internoConsultaTipoRegistro;
    private JInternalFrame internoRegistroEdicion;
    private JInternalFrame internoConsultaEdicion;

    private JInternalFrame internoAltaCategoria;
    private AltaCategoriaPanel panelAltaCategoria;

    //PARA REFRESCAR
    private ModificarUsuarioPanel panelModificarUsuario;
    private ConsultaUsuarioPanel panelConsultaUsuario;
    private AltaPatrocinioPanel panelAltaPatrocinio;
    private ConsultaPatrocinioPanel panelConsultaPatrocinio;
    private AltaEdicionEventoPanel panelAltaEdicion;
    private ConsultaTipoRegistroPanel panelConsultaTipoRegistro;
    private RegistroEdicionEventoPanel panelRegistroEdicion;
    private ConsultaEdicionEventoPanel panelConsultaEdicion;

    public principal() {

        panelPrincipal = new JPanel(new BorderLayout());
        desktopPane = new JDesktopPane();

        panelPrincipal.add(
                desktopPane,
                BorderLayout.CENTER
        );

        inicializarVentanasInternas();
    }

    // INICIALIZAR VENTANAS INTERNAS
    private void inicializarVentanasInternas() {

        // =========================
        // ALTA USUARIO
        // =========================

        AltaUsuarioPanel panelAltaUsuario = new AltaUsuarioPanel();
        internoAltaUsuario =
                crearInterno(
                        "Alta Usuario",
                        panelAltaUsuario.getMainPanel(),
                        20,
                        20
                );

        panelAltaUsuario.setAccionCerrar(
                () -> internoAltaUsuario.setVisible(false)
        );
        desktopPane.add(internoAltaUsuario);


        // =========================
        // MODIFICAR USUARIO
        // =========================

        panelModificarUsuario = new ModificarUsuarioPanel();
        internoModificarUsuario =
                crearInterno(
                        "Modificar Datos de Usuario",
                        panelModificarUsuario.getMainPanel(),
                        60,
                        60
                );

        panelModificarUsuario.setAccionCerrar(
                () -> internoModificarUsuario.setVisible(false)
        );
        desktopPane.add(internoModificarUsuario);


        // =========================
        // CONSULTA USUARIO
        // =========================

        panelConsultaUsuario = new ConsultaUsuarioPanel();
        internoConsultaUsuario =
                crearInterno(
                        "Consulta Usuario",
                        panelConsultaUsuario.getMainPanel(),
                        80,
                        80
                );

        panelConsultaUsuario.setAccionCerrar(
                () -> internoConsultaUsuario.setVisible(false)
        );
        desktopPane.add(internoConsultaUsuario);


        // =========================
        // ALTA INSTITUCIÓN
        // =========================

        AltaInstitucionPanel panelAltaInstitucion = new AltaInstitucionPanel();
        internoAltaInstitucion =
                crearInterno(
                        "Alta Institución",
                        panelAltaInstitucion.getMainPanel(),
                        60,
                        60
                );

        panelAltaInstitucion.setAccionCerrar(
                () -> internoAltaInstitucion.setVisible(false)
        );

        desktopPane.add(internoAltaInstitucion);

        // =========================
        // ALTA EVENTO
        // =========================
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
        desktopPane.add(internoAltaEvento);

        // =========================
        // ALTA PATROCINIO
        // =========================

        panelAltaPatrocinio = new AltaPatrocinioPanel();
        internoAltaPatrocinio =
                crearInterno(
                        "Alta Patrocinio",
                        panelAltaPatrocinio.getMainPanel(),
                        60,
                        60
                );

        panelAltaPatrocinio.setAccionCerrar(
                () -> internoAltaPatrocinio.setVisible(false)
        );

        desktopPane.add(internoAltaPatrocinio);


        // =========================
        // CONSULTA PATROCINIO
        // =========================

        panelConsultaPatrocinio = new ConsultaPatrocinioPanel();
        internoConsultaPatrocinio =
                crearInterno(
                        "Consulta Patrocinio",
                        panelConsultaPatrocinio.getMainPanel(),
                        60,
                        60
                );

        panelConsultaPatrocinio.setAccionCerrar(
                () -> internoConsultaPatrocinio.setVisible(false)
        );

        desktopPane.add(internoConsultaPatrocinio);


        // =========================
        // CONSULTA EVENTO
        // =========================
        panelConsultaEvento = new ConsultaEventoPanel();
        internoConsultaEvento = crearInterno(
                "Consulta Evento",
                panelConsultaEvento, // O panelConsultaEvento.getMainPanel() si creaste un método contenedor
                80,
                80
        );

        desktopPane.add(internoConsultaEvento);

        // =========================
        // ALTA CATEGORIA
        // =========================
        panelAltaCategoria = new AltaCategoriaPanel();
        internoAltaCategoria = crearInterno(
                "Alta Categoría",
                panelAltaCategoria.getMainPanel(),
                80,
                80
        );
        panelAltaCategoria.setAccionCerrar(() -> internoAltaCategoria.setVisible(false));
        desktopPane.add(internoAltaCategoria);


        // =========================
        // ALTA EDICIÓN DE EVENTO
        // =========================
        panelAltaEdicion = new AltaEdicionEventoPanel();
        internoAltaEdicion = crearInterno(
                "Alta Edición de Evento",
                panelAltaEdicion,
                70,
                70
        );
        desktopPane.add(internoAltaEdicion);

        // =========================
        // CONSULTA TIPO DE REGISTRO
        // =========================
        panelConsultaTipoRegistro = new ConsultaTipoRegistroPanel();
        internoConsultaTipoRegistro = crearInterno(
                "Consulta Tipo de Registro",
                panelConsultaTipoRegistro,
                90,
                90
        );
        desktopPane.add(internoConsultaTipoRegistro);

        // =========================
        // REGISTRO A EDICIÓN DE EVENTO
        // =========================
        panelRegistroEdicion = new RegistroEdicionEventoPanel();
        internoRegistroEdicion = crearInterno(
                "Registro a Edición de Evento",
                panelRegistroEdicion,
                100,
                100
        );
        desktopPane.add(internoRegistroEdicion);

        // =========================
        // CONSULTA EDICIÓN DE EVENTO
        // =========================
        panelConsultaEdicion = new ConsultaEdicionEventoPanel();
        internoConsultaEdicion = crearInterno(
                "Consulta Edición de Evento",
                panelConsultaEdicion,
                110,
                110
        );
        desktopPane.add(internoConsultaEdicion);

    }




    // CREAR MENÚ
    private JMenuBar crearMenu() {

        JMenuBar menuBar = new JMenuBar();
        JMenu menuUsuarios = new JMenu("Usuarios");
        JMenu menuEventos = new JMenu("Eventos");
        JMenu menuRegistro = new JMenu("Registros");
        JMenu menuPatrocinio = new JMenu("Patrocinios");
        JMenu menuInstitucion = new JMenu("Instituciones");
        JMenu menuSesion = new JMenu("Sesión");

        JMenuItem altaUsuario = new JMenuItem("Alta Usuario");
        JMenuItem consultaUsuario = new JMenuItem("Consulta Usuario");
        JMenuItem modificarUsuario = new JMenuItem("Modificar Datos de Usuario");
        JMenuItem altaInstitucion = new JMenuItem("Alta Institucion");
        JMenuItem altaEvento = new JMenuItem("Alta Evento");
        JMenuItem consultaEvento = new JMenuItem("Consulta Evento");
        JMenuItem altaEdicion = new JMenuItem("Alta Edicion");
        JMenuItem consultaEdicion = new JMenuItem("Consulta Edicion");
        JMenuItem altaTipoRegistro = new JMenuItem("Alta Tipo Registro");
        JMenuItem consultaTipoRegistro = new JMenuItem("Consulta Tipo Registro");
        JMenuItem altaCategoria = new JMenuItem("Alta Categoria");
        JMenuItem altaPatrocinio = new JMenuItem("Alta Patrocinio");
        JMenuItem consultaPatrocinio = new JMenuItem("Consulta Patrocinio");
        JMenuItem registroEdicionEvento = new JMenuItem("Registro a Edicion de Evento");

        JMenuItem consultaRegistroEvento =
                new JMenuItem(
                        "Consulta de Registro de Edicion de Evento"
                );

        JMenuItem itemSalir =
                new JMenuItem("Salir");

        altaUsuario.addActionListener(e -> {
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

        altaInstitucion.addActionListener(e -> {
            mostrar(internoAltaInstitucion);
        });

        altaPatrocinio.addActionListener(e -> {

            panelAltaPatrocinio.refrescarDatos();

            mostrar(internoAltaPatrocinio);
        });

        consultaPatrocinio.addActionListener(e -> {

            panelConsultaPatrocinio.refrescarDatos();

            mostrar(internoConsultaPatrocinio);
        });

        altaEvento.addActionListener(e -> {
            panelAltaEvento.cargarCategorias();
            mostrar(internoAltaEvento);
        });

        altaEdicion.addActionListener(e -> {
            panelAltaEdicion.cargarEventos();
            mostrar(internoAltaEdicion);
        });

        consultaTipoRegistro.addActionListener(e -> {
            panelConsultaTipoRegistro.cargarEventos();
            mostrar(internoConsultaTipoRegistro);
        });

        consultaEdicion.addActionListener(e -> {
            panelConsultaEdicion.cargarEventos();
            mostrar(internoConsultaEdicion);
        });

        registroEdicionEvento.addActionListener(e -> {
            panelRegistroEdicion.cargarAsistentes();
            panelRegistroEdicion.cargarEventos();
            mostrar(internoRegistroEdicion);
        });

        itemSalir.addActionListener(
                e -> System.exit(0)
        );

        consultaEvento.addActionListener(e -> {
            panelConsultaEvento.cargarComboEventos();
            mostrar(internoConsultaEvento);
        });

        altaCategoria.addActionListener(e -> {
            panelAltaCategoria.cargarArbolCategorias();
            mostrar(internoAltaCategoria);
        });

        menuUsuarios.add(altaUsuario);
        menuUsuarios.add(modificarUsuario);
        menuUsuarios.add(consultaUsuario);

        menuInstitucion.add(altaInstitucion);

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

        menuSesion.add(itemSalir);

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuRegistro);
        menuBar.add(menuPatrocinio);
        menuBar.add(menuInstitucion);
        menuBar.add(menuSesion);

        return menuBar;
    }


    // MOSTRAR INTERNAL FRAME
    private void mostrar(
            JInternalFrame interno
    ) {

        interno.setVisible(true);
        interno.toFront();

        try {

            interno.setSelected(true);

        } catch (PropertyVetoException ignored) {
        }
    }

    // CREAR INTERNAL FRAME
    private JInternalFrame crearInterno(
            String titulo,
            Container contenido,
            int x,
            int y
    ) {

        JInternalFrame interno =
                new JInternalFrame(
                        titulo,
                        true,
                        true,
                        true,
                        true
                );

        interno.setDefaultCloseOperation(
                WindowConstants.HIDE_ON_CLOSE
        );

        interno.setFrameIcon(null);

        interno.setContentPane(
                contenido
        );

        interno.pack();

        interno.setMinimumSize(
                interno.getSize()
        );

        interno.setLocation(
                x,
                y
        );

        interno.setVisible(false);

        return interno;
    }

    // MAIN
    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(() -> {

            principal ventana =
                    new principal();

            JFrame frame =
                    new JFrame(
                            "eventos.uy"
                    );

            frame.setContentPane(
                    ventana.panelPrincipal
            );

            frame.setJMenuBar(
                    ventana.crearMenu()
            );

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setSize(
                    1200,
                    750
            );

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}