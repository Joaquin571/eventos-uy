package swing;

import interfaces.ISistema;
import datatypes.DtPatrocinio;
import javax.swing.*;
import implementacion.Fabrica;

public class ConsultaPatrocinioPanel {

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    private JPanel mainPanel;
    private JPanel panelSeleccion;
    private JPanel panelDatos;
    private JComboBox <String>comboEvento;
    private JComboBox <String>comboEdicion;
    private JComboBox <String>comboPatrocinio;
    private JTextField txtCodigo;
    private JTextField txtFecha;
    private JTextField txtNivel;
    private JTextField txtAporte;
    private JTextField txtRegistroGratuito;
    private JTextField txtInstitución;
    private JTextField txtTipoRegistro;
    private JPanel panelBotones;
    private JButton btnCancelar;

    public  ConsultaPatrocinioPanel() {
        sistema = Fabrica.getInstance().getISistema();

        configurarCampos();
        configurarEventos();
    }
    private void configurarCampos() {
        txtCodigo.setEditable(false);
        txtFecha.setEditable(false);
        txtNivel.setEditable(false);
        txtAporte.setEditable(false);
        txtRegistroGratuito.setEditable(false);
        txtInstitución.setEditable(false);
        txtTipoRegistro.setEditable(false);

        // Temporalmente, hasta implementar Evento y Edición.
        comboEvento.setEnabled(false);
        comboEdicion.setEnabled(false);
    }

    private void configurarEventos() {
        comboPatrocinio.addActionListener(
                e -> seleccionarPatrocinio()
        );

        btnCancelar.addActionListener(e -> {
            limpiarFormulario();
            accionCerrar.run();
        });
    }

    private void limpiarFormulario() {

        txtCodigo.setText("");
        txtAporte.setText("");
        txtFecha.setText("");
        txtNivel.setText("");
        txtRegistroGratuito.setText("");
        txtTipoRegistro.setText("");
        txtInstitución.setText("");

        comboEvento.setSelectedIndex(-1);
        comboEdicion.setSelectedIndex(-1);
        comboPatrocinio.setSelectedIndex(-1);
    }
    public void refrescarDatos() {

        cargarPatrocinios();

        comboEvento.removeAllItems();
        comboEdicion.removeAllItems();

        limpiarFormulario();
    }
    private void cargarPatrocinios() {

        comboPatrocinio.removeAllItems();

        for (DtPatrocinio patrocinio : sistema.listarPatrocinios()) {

            comboPatrocinio.addItem(
                    patrocinio.getCodigoPatrocinio()
            );
        }

        comboPatrocinio.setSelectedIndex(-1);
    }


    private void seleccionarPatrocinio() {

        String codigo =
                (String) comboPatrocinio.getSelectedItem();

        if (codigo == null) {
            return;
        }

        DtPatrocinio patrocinio =
                sistema.consultarPatrocinio(codigo);

        if (patrocinio == null) {
            return;
        }

        txtCodigo.setText(
                patrocinio.getCodigoPatrocinio()
        );

        txtFecha.setText(
                patrocinio.getFecha().toString()
        );

        txtNivel.setText(
                patrocinio.getNivel().toString()
        );

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

        /*
         * Esto lo completamos cuando DtPatrocinio
         * incluya correctamente las relaciones.
         */

        txtInstitución.setText("");
        txtTipoRegistro.setText("");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }
}
