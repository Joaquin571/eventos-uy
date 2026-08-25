package swing;

import java.beans.PropertyVetoException;
import javax.swing.*;
import java.awt.*;

public class principal {

    private JPanel panelPrincipal;
    private JDesktopPane desktopPane;

    private JInternalFrame internoModificarUsuario;

    public principal() {

        panelPrincipal = new JPanel(new BorderLayout());

        desktopPane = new JDesktopPane();
        panelPrincipal.add(desktopPane, BorderLayout.CENTER);

        internoModificarUsuario = crearInternoModificarUsuario();
        desktopPane.add(internoModificarUsuario);
    }

    private JMenuBar crearMenu() {

        JMenuBar menuBar = new JMenuBar();

        JMenu menuUsuarios = new JMenu("Usuarios");
        JMenu menuEventos = new JMenu("Eventos");
        JMenu menuRegistro = new JMenu("Registros");
        JMenu menuPatrocinio = new JMenu("Patrocinios");
        JMenu menuInstitucion = new JMenu("Instituciones");
        JMenu menuSesion = new JMenu("Sesión");

        JMenuItem altaUsuario =
                new JMenuItem("Alta Usuario");

        JMenuItem consultaUsuario =
                new JMenuItem("Consulta Usuario");

        JMenuItem modificarUsuario =
                new JMenuItem("Modificar Datos de Usuario");

        JMenuItem altaInstitucion =
                new JMenuItem("Alta Institucion");

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

        JMenuItem altaPatrocinio =
                new JMenuItem("Alta Patrocinio");

        JMenuItem consultaPatrocinio =
                new JMenuItem("Consulta Patrocinio");

        JMenuItem registroEdicionEvento =
                new JMenuItem("Registro a Edicion de Evento");

        JMenuItem consultaRegistroEvento =
                new JMenuItem("Consulta de Registro de Edicion de Evento");

        JMenuItem itemSalir =
                new JMenuItem("Salir");


        // =========================
        // EVENTOS DE LOS MENU ITEMS
        // =========================

        modificarUsuario.addActionListener(
                e -> mostrar(internoModificarUsuario)
        );

        itemSalir.addActionListener(
                e -> System.exit(0)
        );


        // =========================
        // ARMADO DE LOS MENÚS
        // =========================

        menuUsuarios.add(altaUsuario);
        menuUsuarios.add(consultaUsuario);
        menuUsuarios.add(modificarUsuario);

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


        // =========================
        // AGREGAR MENÚS A LA BARRA
        // =========================

        menuBar.add(menuUsuarios);
        menuBar.add(menuEventos);
        menuBar.add(menuRegistro);
        menuBar.add(menuPatrocinio);
        menuBar.add(menuInstitucion);
        menuBar.add(menuSesion);

        return menuBar;
    }


    /**
     * Muestra un JInternalFrame existente.
     */
    private void mostrar(JInternalFrame interno) {

        interno.setVisible(true);
        interno.toFront();

        try {
            interno.setSelected(true);
        } catch (PropertyVetoException ignored) {
            // La ventana queda visible aunque no pueda seleccionarse.
        }
    }


    /**
     * Crea un JInternalFrame reutilizable para cualquier caso de uso.
     */
    private JInternalFrame crearInterno(
            String titulo,
            Container contenido,
            int x,
            int y) {

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
        interno.setContentPane(contenido);

        interno.pack();

        interno.setMinimumSize(
                interno.getSize()
        );

        interno.setLocation(x, y);
        interno.setVisible(false);

        return interno;
    }


    /**
     * Crea específicamente la ventana de Modificar Usuario.
     */
    private JInternalFrame crearInternoModificarUsuario() {

        ModificarUsuarioPanel panel =
                new ModificarUsuarioPanel();

        JInternalFrame interno =
                crearInterno(
                        "Modificar Datos de Usuario",
                        panel.getMainPanel(),
                        60,
                        60
                );

        panel.setAccionCerrar(
                () -> interno.setVisible(false)
        );

        return interno;
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            principal ventana = new principal();

            JFrame frame =
                    new JFrame("eventos.uy");

            frame.setContentPane(
                    ventana.panelPrincipal
            );

            frame.setJMenuBar(
                    ventana.crearMenu()
            );

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}