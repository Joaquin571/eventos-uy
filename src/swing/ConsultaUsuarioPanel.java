package swing;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ConsultaUsuarioPanel {

    private JPanel mainPanel;
    private JComboBox<DtUsuario> cbUsuarios;

    private JLabel lblTipoUsuario;
    private JLabel lblNickname;
    private JLabel lblNombre;
    private JLabel lblCorreo;
    private JLabel lblEspecial1;
    private JLabel lblEspecial2;

    private JButton btnCerrar;

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    public ConsultaUsuarioPanel() {

        sistema = Fabrica.getInstance().getISistema();

        armarUI();
        configurarEventos();

        limpiarCampos();
    }

    private void configurarEventos() {

        cbUsuarios.addActionListener(e -> cargarDatosUsuario());

        btnCerrar.addActionListener(e -> {
            limpiarCampos();
            accionCerrar.run();
        });
    }

    public void recargarUsuarios() {

        cbUsuarios.removeAllItems();

        Collection<DtUsuario> usuarios = sistema.listarUsuarios();

        for (DtUsuario usuario : usuarios) {
            cbUsuarios.addItem(usuario);
        }

        cbUsuarios.setSelectedIndex(-1);
        limpiarCampos();
    }

    private void cargarDatosUsuario() {

        DtUsuario seleccionado =
                (DtUsuario) cbUsuarios.getSelectedItem();

        if (seleccionado == null) {
            limpiarCampos();
            return;
        }

        DtUsuario completo =
                sistema.consultarUsuario(
                        seleccionado.getNickname()
                );

        if (completo == null) {
            limpiarCampos();
            return;
        }

        lblNickname.setText(completo.getNickname());
        lblNombre.setText(completo.getNombre());
        lblCorreo.setText(completo.getCorreoElectronico());

        if (completo instanceof DtAsistente) {

            DtAsistente asistente =
                    (DtAsistente) completo;

            lblTipoUsuario.setText("Asistente");

            lblEspecial1.setText(
                    "Apellido: " +
                            asistente.getApellido()
            );

            lblEspecial2.setText(
                    "Fecha Nac.: " +
                            (
                                    asistente.getFechaNacimiento() != null
                                            ? asistente.getFechaNacimiento().toString()
                                            : "-"
                            )
            );

        } else if (completo instanceof DtOrganizador) {

            DtOrganizador organizador =
                    (DtOrganizador) completo;

            lblTipoUsuario.setText("Organizador");

            lblEspecial1.setText(
                    "Sitio Web: " +
                            (
                                    organizador.getSitioWeb() != null
                                            ? organizador.getSitioWeb()
                                            : "-"
                            )
            );

            lblEspecial2.setText(
                    "Descripción: " +
                            (
                                    organizador.getDescripcion() != null
                                            ? organizador.getDescripcion()
                                            : "-"
                            )
            );
        }
    }

    private void limpiarCampos() {

        lblTipoUsuario.setText("-");
        lblNickname.setText("-");
        lblNombre.setText("-");
        lblCorreo.setText("-");
        lblEspecial1.setText("-");
        lblEspecial2.setText("-");
    }

    private void armarUI() {

        mainPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        // =========================
        // PANEL SUPERIOR
        // =========================

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        topPanel.add(
                new JLabel("Seleccionar Usuario:")
        );

        cbUsuarios = new JComboBox<>();

        cbUsuarios.setPreferredSize(
                new Dimension(250, 25)
        );

        topPanel.add(cbUsuarios);

        // =========================
        // PANEL DATOS
        // =========================

        JPanel detailPanel =
                new JPanel(
                        new GridLayout(
                                6,
                                2,
                                5,
                                5
                        )
                );

        detailPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Información del Usuario"
                )
        );

        detailPanel.add(
                new JLabel("Tipo:")
        );

        lblTipoUsuario = new JLabel("-");
        detailPanel.add(lblTipoUsuario);


        detailPanel.add(
                new JLabel("Nickname:")
        );

        lblNickname = new JLabel("-");
        detailPanel.add(lblNickname);


        detailPanel.add(
                new JLabel("Nombre:")
        );

        lblNombre = new JLabel("-");
        detailPanel.add(lblNombre);


        detailPanel.add(
                new JLabel("Correo:")
        );

        lblCorreo = new JLabel("-");
        detailPanel.add(lblCorreo);


        detailPanel.add(
                new JLabel("Dato adicional 1:")
        );

        lblEspecial1 = new JLabel("-");
        detailPanel.add(lblEspecial1);


        detailPanel.add(
                new JLabel("Dato adicional 2:")
        );

        lblEspecial2 = new JLabel("-");
        detailPanel.add(lblEspecial2);

        // =========================
        // BOTÓN CERRAR
        // =========================

        JPanel botPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        btnCerrar =
                new JButton("Cerrar");

        botPanel.add(btnCerrar);

        // =========================
        // ARMAR PANEL
        // =========================

        mainPanel.add(
                topPanel,
                BorderLayout.NORTH
        );

        mainPanel.add(
                detailPanel,
                BorderLayout.CENTER
        );

        mainPanel.add(
                botPanel,
                BorderLayout.SOUTH
        );
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAccionCerrar(
            Runnable accionCerrar
    ) {
        this.accionCerrar = accionCerrar;
    }
}