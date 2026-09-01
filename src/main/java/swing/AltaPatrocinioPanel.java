package swing;

import javax.swing.*;

import clases.Edicion;
import clases.NivelPatrocinio;
import datatypes.DtEdicion;
import datatypes.DtInstitucion;
import datatypes.DtPatrocinio;
import interfaces.ISistema;
import implementacion.Fabrica;

import java.time.LocalDate;


public class AltaPatrocinioPanel {

    private final transient ISistema sistema;
    private transient Runnable accionCerrar = () -> {};

    private JPanel mainPanel;
    private JPanel panelSeleccion;
    private JPanel panelDatosPatrocinio;
    private JPanel panelBotones;
    private JComboBox<String> comboEvento;
    private JComboBox<String> comboEdicion;
    private JComboBox<String> comboInstitucion;
    private JComboBox<String> comboTipoRegistro;
    private JComboBox<String> comboNivel;
    private JTextField txtAporte;
    private JTextField txtCantidadRegistros;
    private JTextField txtCodigo;
    private JButton btnAceptar;
    private JButton btnCancelar;

    public AltaPatrocinioPanel() {
        sistema = Fabrica.getInstance().getISistema();
        configurarEventos();

        cargarNiveles();
        cargarInstituciones();
        refrescarDatos();
    }

    private void configurarEventos() {

        btnAceptar.addActionListener(
                e -> guardarPatrocinio()
        );

        btnCancelar.addActionListener(e -> {
            limpiarFormulario();
            accionCerrar.run();
        });
    }
    private void guardarPatrocinio() {

        String evento = (String) comboEvento.getSelectedItem();
        String edicion = (String) comboEdicion.getSelectedItem();
        String institucion = (String) comboInstitucion.getSelectedItem();
        String tipoRegistro = (String) comboTipoRegistro.getSelectedItem();
        LocalDate fecha = LocalDate.now();
        String aporteTexto = txtAporte.getText().trim();
        String cantidadTexto = txtCantidadRegistros.getText().trim();
        String codigo = txtCodigo.getText().trim();
        String nivel = (String) comboNivel.getSelectedItem();

        if (evento == null || edicion == null || institucion == null || tipoRegistro == null || nivel == null) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Debe seleccionar evento, edición, institución, tipo de registro y nivel.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (codigo.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El código de patrocinio es obligatorio.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        if (aporteTexto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El aporte económico es obligatorio.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }
        if (cantidadTexto.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "La cantidad de registros gratuitos es obligatoria.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        float aporte;
        try {
            aporte = Float.parseFloat(aporteTexto);
            if (aporte <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El aporte económico debe ser un número mayor que 0.",
                    "Alta Patrocinio",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
            int cantidadRegistros;
            try {
                cantidadRegistros =
                        Integer.parseInt(cantidadTexto);

                if (cantidadRegistros < 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        mainPanel,
                        "La cantidad de registros gratuitos debe ser un número entero mayor o igual a 0.",
                        "Alta Patrocinio",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            NivelPatrocinio nivelSeleccionado =
                    NivelPatrocinio.valueOf(nivel);

            DtPatrocinio dt =
                    new DtPatrocinio(
                            fecha,
                            aporte,
                            cantidadRegistros,
                            codigo,
                            nivelSeleccionado,
                            institucion,
                            edicion,
                            tipoRegistro
                    );

            boolean agregado = sistema.altaPatrocinio(dt);

        if (!agregado) {
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "No se pudo crear el patrocinio.",
                    "Alta Patrocinio",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        mostrarExito();
        limpiarFormulario();
        accionCerrar.run();
    }


    private void mostrarExito() {

        JOptionPane.showMessageDialog(
                mainPanel,
                "Patrocinio creado correctamente.",
                "Alta Patrocinio",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private void limpiarFormulario() {

        txtAporte.setText("");
        txtCantidadRegistros.setText("");
        txtCodigo.setText("");

        comboEvento.setSelectedIndex(-1);
        comboEdicion.setSelectedIndex(-1);
        comboInstitucion.setSelectedIndex(-1);
        comboTipoRegistro.setSelectedIndex(-1);
        comboNivel.setSelectedIndex(-1);
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public void setAccionCerrar(Runnable accionCerrar) {
        this.accionCerrar = accionCerrar;
    }

    public void cargarNiveles(){
        comboNivel.removeAllItems();
        for (NivelPatrocinio nivel : NivelPatrocinio.values()) {
            comboNivel.addItem(nivel.name());
        }
        comboNivel.setSelectedIndex(-1);
    }

    private void cargarInstituciones() {

        comboInstitucion.removeAllItems();

        for (DtInstitucion institucion : sistema.listarInstituciones()) {
            comboInstitucion.addItem(institucion.getNombre());
        }

        comboInstitucion.setSelectedIndex(-1);
    }

    public void refrescarDatos() {

        cargarInstituciones();
        cargarNiveles();

        comboEdicion.removeAllItems();
        comboTipoRegistro.removeAllItems();

        comboEvento.setSelectedIndex(-1);
        comboEdicion.setSelectedIndex(-1);
        comboInstitucion.setSelectedIndex(-1);
        comboTipoRegistro.setSelectedIndex(-1);
        comboNivel.setSelectedIndex(-1);
    }
}
