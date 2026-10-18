package swing;

import datatypes.DtAsistente;
import datatypes.DtOrganizador;
import datatypes.DtUsuario;
import datatypes.DtEdicion;
import datatypes.DtRegistro;

import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class ConsultaUsuarioPanel {

    private final principal ventanaPrincipal;

    private JPanel mainPanel;
    private JComboBox<DtUsuario> cbUsuarios;

    private JLabel lblTipoUsuario;
    private JLabel lblNickname;
    private JLabel lblNombre;
    private JLabel lblCorreo;
    private JLabel lblEspecial1;
    private JLabel lblEspecial2;

    private JList<Object> listaAsociados;
    private DefaultListModel<Object> modeloAsociados;
    private JLabel lblAsociados;

    private JButton btnCerrar;

    private final transient ISistema sistema;

    private transient Runnable accionCerrar =
            () -> {
            };

    public ConsultaUsuarioPanel(
            principal ventanaPrincipal
    ) {

        sistema =
                Fabrica
                        .getInstance()
                        .getISistema();

        this.ventanaPrincipal =
                ventanaPrincipal;

        armarUI();

        configurarEventos();

        limpiarCampos();
    }

    private String buscarEventoDeEdicion(String nombreEdicion){
        for (var evento : sistema.listarEventos()) {
            Collection<DtEdicion> ediciones = sistema.obtenerEdicionesEvento(evento.getNombre());
            for (DtEdicion edicion : ediciones) {
                if (edicion.getIdNombre().equals(nombreEdicion)) {
                    return evento.getNombre();
                }
            }
        }

        return null;
    }
    // =====================================================
    // EVENTOS
    // =====================================================

    private void configurarEventos() {

        cbUsuarios.addActionListener(
                e -> cargarDatosUsuario()
        );

        btnCerrar.addActionListener(
                e -> {

                    limpiarCampos();

                    accionCerrar.run();
                }
        );

        // =================================================
        // DOBLE CLICK SOBRE REGISTROS / EDICIONES
        // =================================================

        listaAsociados.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        if (e.getClickCount() == 2) {

                            abrirAsociadoSeleccionado();
                        }
                    }
                }
        );
    }

    // =====================================================
    // DOBLE CLICK EN ELEMENTO ASOCIADO
    // =====================================================

    private void abrirAsociadoSeleccionado() {

        Object seleccionado =
                listaAsociados.getSelectedValue();

        if (seleccionado == null) {
            return;
        }

        DtUsuario usuario =
                (DtUsuario)
                        cbUsuarios
                                .getSelectedItem();

        if (usuario == null) {
            return;
        }

        // =================================================
        // ASISTENTE -> CONSULTA REGISTRO
        // =================================================

        if (seleccionado instanceof DtRegistro registro) {

            String nicknameAsistente =
                    usuario.getNickname();

            String nombreEdicion =
                    registro.getNombreEdicion();

            if (nombreEdicion == null
                    || nombreEdicion.isBlank()) {

                return;
            }

            ventanaPrincipal
                    .navegarAConsultaRegistro(
                            nicknameAsistente,
                            nombreEdicion
                    );

            return;
        }

        if (seleccionado instanceof DtEdicion edicion) {

            String nombreEvento =
                    buscarEventoDeEdicion(
                            edicion.getIdNombre()
                    );

            if (nombreEvento == null) {

                JOptionPane.showMessageDialog(
                        mainPanel,
                        "No se pudo determinar el evento de la edición seleccionada.",
                        "Consulta Usuario",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            ventanaPrincipal.navegarAConsultaEdicion(
                    nombreEvento,
                    edicion.getIdNombre()
            );
        }
    }

    // =====================================================
    // REFRESCAR USUARIOS
    // =====================================================

    public void recargarUsuarios() {

        cbUsuarios.removeAllItems();

        Collection<DtUsuario> usuarios =
                sistema.listarUsuarios();

        for (DtUsuario usuario : usuarios) {

            cbUsuarios.addItem(usuario);
        }

        cbUsuarios.setSelectedIndex(-1);

        limpiarCampos();
    }

    // =====================================================
    // CARGAR DATOS DEL USUARIO
    // =====================================================

    private void cargarDatosUsuario() {

        DtUsuario seleccionado =
                (DtUsuario)
                        cbUsuarios
                                .getSelectedItem();

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

        lblNickname.setText(
                completo.getNickname()
        );

        lblNombre.setText(
                completo.getNombre()
        );

        lblCorreo.setText(
                completo.getCorreoElectronico()
        );

        // =================================================
        // ASISTENTE
        // =================================================

        if (completo instanceof DtAsistente asistente) {

            lblTipoUsuario.setText(
                    "Asistente"
            );

            lblEspecial1.setText(
                    "Apellido: "
                            + asistente.getApellido()
            );

            lblEspecial2.setText(
                    "Fecha Nac.: "
                            + (
                            asistente.getFechaNacimiento()
                                    != null
                                    ? asistente
                                    .getFechaNacimiento()
                                    .toString()
                                    : "-"
                    )
            );

            lblAsociados.setText(
                    "Registros asociados:"
            );

            modeloAsociados.clear();

            Collection<DtRegistro> registros =
                    sistema.obtenerRegistrosAsistente(
                            asistente.getNickname()
                    );

            for (DtRegistro registro :
                    registros) {

                modeloAsociados.addElement(
                        registro
                );
            }

        }

        // =================================================
        // ORGANIZADOR
        // =================================================

        else if (
                completo instanceof DtOrganizador organizador
        ) {

            lblTipoUsuario.setText(
                    "Organizador"
            );

            lblEspecial1.setText(
                    "Sitio Web: "
                            + (
                            organizador.getSitioWeb()
                                    != null
                                    ? organizador.getSitioWeb()
                                    : "-"
                    )
            );

            lblEspecial2.setText(
                    "Descripción: "
                            + (
                            organizador.getDescripcion()
                                    != null
                                    ? organizador.getDescripcion()
                                    : "-"
                    )
            );

            lblAsociados.setText(
                    "Ediciones organizadas:"
            );

            modeloAsociados.clear();

            Collection<DtEdicion> ediciones =
                    sistema.obtenerEdicionesOrganizador(
                            organizador.getNickname()
                    );

            for (DtEdicion edicion :
                    ediciones) {

                modeloAsociados.addElement(
                        edicion
                );
            }
        }
    }

    // =====================================================
    // LIMPIAR
    // =====================================================

    private void limpiarCampos() {

        lblTipoUsuario.setText("-");
        lblNickname.setText("-");
        lblNombre.setText("-");
        lblCorreo.setText("-");
        lblEspecial1.setText("-");
        lblEspecial2.setText("-");

        lblAsociados.setText(
                "Asociados:"
        );

        modeloAsociados.clear();
    }

    // =====================================================
    // ARMAR UI
    // =====================================================

    private void armarUI() {

        mainPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        mainPanel.setBorder(
                BorderFactory
                        .createEmptyBorder(
                                15,
                                15,
                                15,
                                15
                        )
        );

        // =================================================
        // PANEL SUPERIOR
        // =================================================

        JPanel topPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        topPanel.add(
                new JLabel(
                        "Seleccionar Usuario:"
                )
        );

        cbUsuarios =
                new JComboBox<>();

        cbUsuarios.setPreferredSize(
                new Dimension(
                        250,
                        25
                )
        );

        topPanel.add(
                cbUsuarios
        );


        // =================================================
        // PANEL DATOS
        // =================================================

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
                BorderFactory
                        .createTitledBorder(
                                "Información del Usuario"
                        )
        );

        detailPanel.add(
                new JLabel("Tipo:")
        );

        lblTipoUsuario =
                new JLabel("-");

        detailPanel.add(
                lblTipoUsuario
        );

        detailPanel.add(
                new JLabel("Nickname:")
        );

        lblNickname =
                new JLabel("-");

        detailPanel.add(
                lblNickname
        );

        detailPanel.add(
                new JLabel("Nombre:")
        );

        lblNombre =
                new JLabel("-");

        detailPanel.add(
                lblNombre
        );

        detailPanel.add(
                new JLabel("Correo:")
        );

        lblCorreo =
                new JLabel("-");

        detailPanel.add(
                lblCorreo
        );

        detailPanel.add(
                new JLabel(
                        "Dato adicional 1:"
                )
        );

        lblEspecial1 =
                new JLabel("-");

        detailPanel.add(
                lblEspecial1
        );

        detailPanel.add(
                new JLabel(
                        "Dato adicional 2:"
                )
        );

        lblEspecial2 =
                new JLabel("-");

        detailPanel.add(
                lblEspecial2
        );

        // =================================================
        // PANEL ASOCIADOS
        // =================================================

        JPanel asociadosPanel =
                new JPanel(
                        new BorderLayout(
                                5,
                                5
                        )
                );

        asociadosPanel.setBorder(
                BorderFactory
                        .createTitledBorder(
                                "Información asociada"
                        )
        );

        lblAsociados =
                new JLabel(
                        "Asociados:"
                );

        modeloAsociados =
                new DefaultListModel<>();

        listaAsociados =
                new JList<>(
                        modeloAsociados
                );

        listaAsociados.setSelectionMode(
                ListSelectionModel
                        .SINGLE_SELECTION
        );

        listaAsociados.setToolTipText(
                "Doble clic para ver el detalle"
        );

        JScrollPane scrollAsociados =
                new JScrollPane(
                        listaAsociados
                );

        scrollAsociados
                .setPreferredSize(
                        new Dimension(
                                500,
                                150
                        )
                );

        asociadosPanel.add(
                lblAsociados,
                BorderLayout.NORTH
        );

        asociadosPanel.add(
                scrollAsociados,
                BorderLayout.CENTER
        );

        // =================================================
        // BOTÓN CERRAR
        // =================================================

        JPanel botPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT
                        )
                );

        btnCerrar =
                new JButton(
                        "Cerrar"
                );

        botPanel.add(
                btnCerrar
        );

        // =================================================
        // ARMAR PANEL
        // =================================================

        mainPanel.add(
                topPanel,
                BorderLayout.NORTH
        );

        JPanel centroPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        centroPanel.add(
                detailPanel,
                BorderLayout.NORTH
        );

        centroPanel.add(
                asociadosPanel,
                BorderLayout.CENTER
        );

        mainPanel.add(
                centroPanel,
                BorderLayout.CENTER
        );

        mainPanel.add(
                botPanel,
                BorderLayout.SOUTH
        );
    }

    // =====================================================
    // GETTERS / CIERRE
    // =====================================================

    public JPanel getMainPanel() {

        return mainPanel;
    }

    public void setAccionCerrar(
            Runnable accionCerrar
    ) {

        this.accionCerrar =
                accionCerrar;
    }
}