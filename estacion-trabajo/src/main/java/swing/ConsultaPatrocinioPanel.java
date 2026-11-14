package swing;

import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtPatrocinio;
import datatypes.TipoEntidadImagen;
import interfaces.ISistema;
import implementacion.Fabrica;

import javax.swing.*;

public class ConsultaPatrocinioPanel {

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    private JPanel mainPanel;
    private final ImagenSwing imagen = new ImagenSwing(false);
    private JPanel panelSeleccion;
    private JPanel panelDatos;

    private JComboBox<String> comboEvento;
    private JComboBox<String> comboEdicion;
    private JComboBox<String> comboPatrocinio;

    private JTextField txtCodigo;
    private JTextField txtFecha;
    private JTextField txtNivel;
    private JTextField txtAporte;
    private JTextField txtRegistroGratuito;
    private JTextField txtInstitución;
    private JTextField txtTipoRegistro;

    private JPanel panelBotones;
    private JButton btnCancelar;

    public ConsultaPatrocinioPanel() {

        sistema =
                Fabrica.getInstance()
                        .getISistema();

        armarUI();
        configurarCampos();
        configurarEventos();
    }

    // =====================================================
    // CONFIGURACIÓN CAMPOS
    // =====================================================

    private void armarUI() {
        comboEvento = new JComboBox<>(); comboEdicion = new JComboBox<>(); comboPatrocinio = new JComboBox<>();
        txtCodigo = new JTextField(24); txtFecha = new JTextField(24); txtNivel = new JTextField(24); txtAporte = new JTextField(24);
        txtRegistroGratuito = new JTextField(24); txtInstitución = new JTextField(24); txtTipoRegistro = new JTextField(24);
        btnCancelar = new JButton("Cerrar");
        panelSeleccion = FormularioSwing.campos("Evento:", comboEvento, "Edición:", comboEdicion, "Patrocinio:", comboPatrocinio);
        panelDatos = FormularioSwing.campos("Código:", txtCodigo, "Fecha:", txtFecha, "Nivel:", txtNivel, "Aporte:", txtAporte, "Registros gratuitos:", txtRegistroGratuito, "Institución:", txtInstitución, "Tipo de registro:", txtTipoRegistro);
        panelBotones = FormularioSwing.botones(btnCancelar);
        mainPanel = FormularioSwing.componer(panelBotones, panelSeleccion, panelDatos, imagen);
    }

    private void configurarCampos() {

        txtCodigo.setEditable(false);
        txtFecha.setEditable(false);
        txtNivel.setEditable(false);
        txtAporte.setEditable(false);
        txtRegistroGratuito.setEditable(false);
        txtInstitución.setEditable(false);
        txtTipoRegistro.setEditable(false);

        comboEvento.setEnabled(true);
        comboEdicion.setEnabled(true);
        comboPatrocinio.setEnabled(true);
    }

    // =====================================================
    // EVENTOS DE LA PANTALLA
    // =====================================================

    private void configurarEventos() {

        comboEvento.addActionListener(
                e -> cargarEdiciones()
        );

        comboEdicion.addActionListener(
                e -> cargarPatrocinios()
        );

        comboPatrocinio.addActionListener(
                e -> seleccionarPatrocinio()
        );

        btnCancelar.addActionListener(e -> {

            limpiarFormulario();

            accionCerrar.run();
        });
    }

    // =====================================================
    // REFRESCAR
    // =====================================================

    public void refrescarDatos() {

        limpiarFormulario();

        cargarEventos();
    }

    // =====================================================
    // CARGAR EVENTOS
    // =====================================================

    private void cargarEventos() {

        comboEvento.removeAllItems();

        for (DtEvento evento :
                sistema.listarEventos()) {

            comboEvento.addItem(
                    evento.getNombre()
            );
        }

        comboEvento.setSelectedIndex(-1);
    }

    // =====================================================
    // CARGAR EDICIONES
    // =====================================================

    private void cargarEdiciones() {

        comboEdicion.removeAllItems();
        comboPatrocinio.removeAllItems();

        limpiarDatosPatrocinio();

        String nombreEvento =
                (String) comboEvento.getSelectedItem();

        if (nombreEvento == null) {

            comboEdicion.setSelectedIndex(-1);
            comboPatrocinio.setSelectedIndex(-1);

            return;
        }

        for (DtEdicion edicion :
                sistema.obtenerEdicionesEvento(
                        nombreEvento
                )) {

            comboEdicion.addItem(
                    edicion.getIdNombre()
            );
        }

        comboEdicion.setSelectedIndex(-1);
        comboPatrocinio.setSelectedIndex(-1);
    }

    // =====================================================
    // CARGAR PATROCINIOS DE LA EDICIÓN
    // =====================================================

    private void cargarPatrocinios() {

        comboPatrocinio.removeAllItems();

        limpiarDatosPatrocinio();

        String nombreEdicion =
                (String) comboEdicion.getSelectedItem();

        if (nombreEdicion == null) {

            comboPatrocinio.setSelectedIndex(-1);

            return;
        }

        for (DtPatrocinio patrocinio :
                sistema.obtenerPatrociniosEdicion(
                        nombreEdicion
                )) {

            comboPatrocinio.addItem(
                    patrocinio.getCodigoPatrocinio()
            );
        }

        comboPatrocinio.setSelectedIndex(-1);
    }

    // =====================================================
    // MOSTRAR PATROCINIO
    // =====================================================

    private void seleccionarPatrocinio() {

        String codigo =
                (String) comboPatrocinio.getSelectedItem();

        if (codigo == null) {

            limpiarDatosPatrocinio();

            return;
        }

        DtPatrocinio patrocinio =
                sistema.consultarPatrocinio(
                        codigo
                );

        if (patrocinio == null) {

            limpiarDatosPatrocinio();

            return;
        }

        imagen.cargar(sistema, TipoEntidadImagen.INSTITUCION, patrocinio.getNombreInstituto());
        txtCodigo.setText(
                patrocinio.getCodigoPatrocinio()
        );

        if (patrocinio.getFecha() != null) {

            txtFecha.setText(
                    patrocinio.getFecha().toString()
            );

        } else {

            txtFecha.setText("");
        }

        if (patrocinio.getNivel() != null) {

            txtNivel.setText(
                    patrocinio.getNivel().toString()
            );

        } else {

            txtNivel.setText("");
        }

        txtAporte.setText(
                String.valueOf(
                        patrocinio.getMontoAporte()
                )
        );

        txtRegistroGratuito.setText(
                String.valueOf(
                        patrocinio.getCantRegistrosGrat()
                )
        );

        if (patrocinio.getNombreInstituto() != null) {

            txtInstitución.setText(
                    patrocinio.getNombreInstituto()
            );

        } else {

            txtInstitución.setText("");
        }

        if (patrocinio.getNombreTipoRegistro() != null) {

            txtTipoRegistro.setText(
                    patrocinio.getNombreTipoRegistro()
            );

        } else {

            txtTipoRegistro.setText("");
        }
    }

    // =====================================================
    // LIMPIAR DATOS DEL PATROCINIO
    // =====================================================

    private void limpiarDatosPatrocinio() {
        imagen.mostrar(null);

        txtCodigo.setText("");
        txtFecha.setText("");
        txtNivel.setText("");
        txtAporte.setText("");
        txtRegistroGratuito.setText("");
        txtInstitución.setText("");
        txtTipoRegistro.setText("");
    }

    // =====================================================
    // LIMPIAR TODO
    // =====================================================

    private void limpiarFormulario() {

        limpiarDatosPatrocinio();

        comboEvento.removeAllItems();
        comboEdicion.removeAllItems();
        comboPatrocinio.removeAllItems();

        comboEvento.setSelectedIndex(-1);
        comboEdicion.setSelectedIndex(-1);
        comboPatrocinio.setSelectedIndex(-1);
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