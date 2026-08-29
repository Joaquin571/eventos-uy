package swing;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

public class principal {

    private JPanel panelPrincipal;
    private JDesktopPane desktopPane;

    // =========================
    // INTERNAL FRAMES
    // =========================

    private JInternalFrame internoAltaUsuario;
    private JInternalFrame internoModificarUsuario;
    private JInternalFrame internoConsultaUsuario;

    private JInternalFrame internoAltaInstitucion;

    private JInternalFrame internoAltaPatrocinio;
    private JInternalFrame internoConsultaPatrocinio;


    // =========================
    // PANELES QUE NECESITAMOS
    // CONSERVAR COMO ATRIBUTO
    // =========================

    private ModificarUsuarioPanel panelModificarUsuario;
    private ConsultaUsuarioPanel panelConsultaUsuario;

    private AltaPatrocinioPanel panelAltaPatrocinio;
    private ConsultaPatrocinioPanel panelConsultaPatrocinio;


    public principal() {

        panelPrincipal =
                new JPanel(new BorderLayout());

        desktopPane =
                new JDesktopPane();

        panelPrincipal.add(
                desktopPane,
                BorderLayout.CENTER
        );

        inicializarVentanasInternas();
    }


    // =========================================================
    // INICIALIZAR VENTANAS INTERNAS
    // =========================================================

    private void inicializarVentanasInternas() {

        // =========================
        // ALTA USUARIO
        // =========================

        AltaUsuarioPanel panelAltaUsuario =
                new AltaUsuarioPanel();

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

        desktopPane.add(
                internoAltaUsuario
        );


        // =========================
        // MODIFICAR USUARIO
        // =========================

        panelModificarUsuario =
                new ModificarUsuarioPanel();

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

        desktopPane.add(
                internoModificarUsuario
        );


        // =========================
        // CONSULTA USUARIO
        // =========================

        panelConsultaUsuario =
                new ConsultaUsuarioPanel();

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

        desktopPane.add(
                internoConsultaUsuario
        );


        // =========================
        // ALTA INSTITUCIÓN
        // =========================

        AltaInstitucionPanel panelAltaInstitucion =
                new AltaInstitucionPanel();

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

        desktopPane.add(
                internoAltaInstitucion
        );


        // =========================
        // ALTA PATROCINIO
        // =========================

        panelAltaPatrocinio =
                new AltaPatrocinioPanel();

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

        desktopPane.add(
                internoAltaPatrocinio
        );


        // =========================
        // CONSULTA PATROCINIO
        // =========================

        panelConsultaPatrocinio =
                new ConsultaPatrocinioPanel();

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

        desktopPane.add(
                internoConsultaPatrocinio
        );
    }


    // =========================================================
    // CREAR MENÚ
    // =========================================================

    private JMenuBar crearMenu() {

        JMenuBar menuBar =
                new JMenuBar();


        // =========================
        // MENÚS PRINCIPALES
        // =========================

        JMenu menuUsuarios =
                new JMenu("Usuarios");

        JMenu menuEventos =
                new JMenu("Eventos");

        JMenu menuRegistro =
                new JMenu("Registros");

        JMenu menuPatrocinio =
                new JMenu("Patrocinios");

        JMenu menuInstitucion =
                new JMenu("Instituciones");

        JMenu menuSesion =
                new JMenu("Sesión");


        // =========================
        // USUARIOS
        // =========================

        JMenuItem altaUsuario =
                new JMenuItem("Alta Usuario");

        JMenuItem consultaUsuario =
                new JMenuItem("Consulta Usuario");

        JMenuItem modificarUsuario =
                new JMenuItem("Modificar Datos de Usuario");


        // =========================
        // INSTITUCIONES
        // =========================

        JMenuItem altaInstitucion =
                new JMenuItem("Alta Institucion");


        // =========================
        // EVENTOS
        // =========================

        JMenuItem altaEvento =
                new JMenuItem("Alta Evento");

        JMenuItem consultaEvento =
                new JMenuItem("Consulta Evento");

        JMenuItem altaEdicion =
                new JMenuItem("Alta Edicion");

        JMenuItem consultaEdicion =
                new JMenuItem("Consulta Edicion");

        JMenuItem altaTipoRegistro =
                new JMenuItem("Alta Tipo Registro");

        JMenuItem consultaTipoRegistro =
                new JMenuItem("Consulta Tipo Registro");

        JMenuItem altaCategoria =
                new JMenuItem("Alta Categoria");


        // =========================
        // PATROCINIOS
        // =========================

        JMenuItem altaPatrocinio =
                new JMenuItem("Alta Patrocinio");

        JMenuItem consultaPatrocinio =
                new JMenuItem("Consulta Patrocinio");


        // =========================
        // REGISTROS
        // =========================

        JMenuItem registroEdicionEvento =
                new JMenuItem(
                        "Registro a Edicion de Evento"
                );

        JMenuItem consultaRegistroEvento =
                new JMenuItem(
                        "Consulta de Registro de Edicion de Evento"
                );


        // =========================
        // SESIÓN
        // =========================

        JMenuItem itemSalir =
                new JMenuItem("Salir");


        // =====================================================
        // LISTENERS USUARIOS
        // =====================================================

        altaUsuario.addActionListener(e -> {
            mostrar(internoAltaUsuario);
        });


        modificarUsuario.addActionListener(e -> {

            panelModificarUsuario.refrescarUsuarios();

            mostrar(internoModificarUsuario);
        });


        consultaUsuario.addActionListener(e -> {

            panelConsultaUsuario.recargarUsuarios();

            mostrar(internoConsultaUsuario);
        });


        // =====================================================
        // LISTENER INSTITUCIÓN
        // =====================================================

        altaInstitucion.addActionListener(e -> {
            mostrar(internoAltaInstitucion);
        });


        // =====================================================
        // LISTENERS PATROCINIOS
        // =====================================================

        altaPatrocinio.addActionListener(e -> {

            panelAltaPatrocinio.refrescarDatos();

            mostrar(internoAltaPatrocinio);
        });


        consultaPatrocinio.addActionListener(e -> {

            panelConsultaPatrocinio.refrescarDatos();

            mostrar(internoConsultaPatrocinio);
        });


        // =====================================================
        // SALIR
        // =====================================================

        itemSalir.addActionListener(
                e -> System.exit(0)
        );


        // =====================================================
        // ARMADO MENÚ USUARIOS
        // =====================================================

        menuUsuarios.add(altaUsuario);
        menuUsuarios.add(modificarUsuario);
        menuUsuarios.add(consultaUsuario);


        // =====================================================
        // ARMADO MENÚ INSTITUCIONES
        // =====================================================

        menuInstitucion.add(altaInstitucion);


        // =====================================================
        // ARMADO MENÚ EVENTOS
        // =====================================================

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


        // =====================================================
        // ARMADO MENÚ REGISTROS
        // =====================================================

        menuRegistro.add(registroEdicionEvento);
        menuRegistro.add(consultaRegistroEvento);


        // =====================================================
        // ARMADO MENÚ PATROCINIOS
        // =====================================================

        menuPatrocinio.add(altaPatrocinio);
        menuPatrocinio.add(consultaPatrocinio);


        // =====================================================
        // ARMADO MENÚ SESIÓN
        // =====================================================

        menuSesion.add(itemSalir);


        // =====================================================
        // AGREGAR MENÚS A LA BARRA
        // =====================================================

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuRegistro);
        menuBar.add(menuPatrocinio);
        menuBar.add(menuInstitucion);
        menuBar.add(menuSesion);

        return menuBar;
    }


    // =========================================================
    // MOSTRAR INTERNAL FRAME
    // =========================================================

    private void mostrar(
            JInternalFrame interno
    ) {

        interno.setVisible(true);
        interno.toFront();

        try {

            interno.setSelected(true);

        } catch (PropertyVetoException ignored) {

            // Sigue visible aunque no se pueda seleccionar.
        }
    }


    // =========================================================
    // CREAR INTERNAL FRAME
    // =========================================================

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


    // =========================================================
    // MAIN
    // =========================================================

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