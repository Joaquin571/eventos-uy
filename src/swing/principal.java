package swing;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

public class principal {

    private JPanel panelPrincipal;
    private JDesktopPane desktopPane;

    private JInternalFrame internoAltaUsuario;
    private JInternalFrame internoModificarUsuario;
    private JInternalFrame internoConsultaUsuario;

    private ModificarUsuarioPanel panelModificarUsuario;
    private ConsultaUsuarioPanel panelConsultaUsuario;


    public principal() {

        panelPrincipal = new JPanel(new BorderLayout());

        desktopPane = new JDesktopPane();

        panelPrincipal.add(
                desktopPane,
                BorderLayout.CENTER
        );

        inicializarVentanasInternas();
    }


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

        desktopPane.add(internoAltaUsuario);


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

        desktopPane.add(internoModificarUsuario);


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

        desktopPane.add(internoConsultaUsuario);
    }


    private JMenuBar crearMenu() {

        JMenuBar menuBar =
                new JMenuBar();

        JMenu menuUsuarios =
                new JMenu("Usuarios");


        JMenuItem altaUsuario =
                new JMenuItem("Alta Usuario");

        JMenuItem modificarUsuario =
                new JMenuItem("Modificar Usuario");

        JMenuItem consultaUsuario =
                new JMenuItem("Consulta Usuario");


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


        menuUsuarios.add(altaUsuario);
        menuUsuarios.add(modificarUsuario);
        menuUsuarios.add(consultaUsuario);

        menuBar.add(menuUsuarios);

        return menuBar;
    }


    private void mostrar(JInternalFrame interno) {

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


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            principal ventana =
                    new principal();

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

            frame.setSize(
                    1200,
                    750
            );

            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}