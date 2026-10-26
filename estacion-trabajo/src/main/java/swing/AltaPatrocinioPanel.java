package swing;

import javax.swing.*;

import clases.NivelPatrocinio;
import datatypes.DtEdicion;
import datatypes.DtInstitucion;
import datatypes.DtPatrocinio;
import datatypes.DtEvento;
import datatypes.DtTipoRegistro;
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

        sistema =
                Fabrica.getInstance()
                        .getISistema();

        configurarEventos();

        refrescarDatos();
    }

    // =====================================================
    // CONFIGURACIÓN DE EVENTOS
    // =====================================================

    private void configurarEventos() {

        btnAceptar.addActionListener(
                e -> guardarPatrocinio()
        );

        btnCancelar.addActionListener(e -> {

            limpiarFormulario();

            accionCerrar.run();
        });

        comboEvento.addActionListener(e -> {

            cargarEdiciones();
        });

        comboEdicion.addActionListener(e -> {

            cargarTiposRegistro();
        });
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
        comboTipoRegistro.removeAllItems();

        String nombreEvento =
                (String) comboEvento.getSelectedItem();

        if (nombreEvento == null) {

            comboEdicion.setSelectedIndex(-1);
            comboTipoRegistro.setSelectedIndex(-1);

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
        comboTipoRegistro.setSelectedIndex(-1);
    }

    // =====================================================
    // CARGAR TIPOS DE REGISTRO
    // =====================================================

    private void cargarTiposRegistro() {

        comboTipoRegistro.removeAllItems();

        String nombreEdicion =
                (String) comboEdicion.getSelectedItem();

        if (nombreEdicion == null) {

            comboTipoRegistro.setSelectedIndex(-1);

            return;
        }

        for (DtTipoRegistro tipo :
                sistema.obtenerTiposRegistroEdicion(
                        nombreEdicion
                )) {

            comboTipoRegistro.addItem(
                    tipo.getIdNombre()
            );
        }

        comboTipoRegistro.setSelectedIndex(-1);
    }

    // =====================================================
    // CARGAR INSTITUCIONES
    // =====================================================

    private void cargarInstituciones() {

        comboInstitucion.removeAllItems();

        for (DtInstitucion institucion :
                sistema.listarInstituciones()) {

            comboInstitucion.addItem(
                    institucion.getNombre()
            );
        }

        comboInstitucion.setSelectedIndex(-1);
    }

    // =====================================================
    // CARGAR NIVELES
    // =====================================================

    private void cargarNiveles() {

        comboNivel.removeAllItems();

        for (NivelPatrocinio nivel :
                NivelPatrocinio.values()) {

            comboNivel.addItem(
                    nivel.name()
            );
        }

        comboNivel.setSelectedIndex(-1);
    }

    // =====================================================
    // GUARDAR PATROCINIO
    // =====================================================

    private void guardarPatrocinio() {

        String evento =
                (String) comboEvento.getSelectedItem();

        String edicion =
                (String) comboEdicion.getSelectedItem();

        String institucion =
                (String) comboInstitucion.getSelectedItem();

        String tipoRegistro =
                (String) comboTipoRegistro.getSelectedItem();

        String nivel =
                (String) comboNivel.getSelectedItem();

        String aporteTexto =
                txtAporte.getText().trim();

        String cantidadTexto =
                txtCantidadRegistros
                        .getText()
                        .trim();

        String codigo =
                txtCodigo.getText().trim();

        LocalDate fecha =
                LocalDate.now();

        // =================================================
        // VALIDAR COMBOS
        // =================================================

        if (evento == null
                || edicion == null
                || institucion == null
                || tipoRegistro == null
                || nivel == null) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Debe seleccionar evento, edición, institución, tipo de registro y nivel.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // =================================================
        // VALIDAR CÓDIGO
        // =================================================

        if (codigo.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El código de patrocinio es obligatorio.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // =================================================
        // VALIDAR APORTE
        // =================================================

        if (aporteTexto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El aporte económico es obligatorio.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // =================================================
        // VALIDAR CANTIDAD
        // =================================================

        if (cantidadTexto.isEmpty()) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "La cantidad de registros gratuitos es obligatoria.",
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // =================================================
        // CONVERTIR APORTE
        // =================================================

        float aporte;

        try {

            aporte =
                    Float.parseFloat(
                            aporteTexto
                    );

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

        // =================================================
        // CONVERTIR CANTIDAD
        // =================================================

        int cantidadRegistros;

        try {

            cantidadRegistros =
                    Integer.parseInt(
                            cantidadTexto
                    );

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

        // =================================================
        // NIVEL
        // =================================================

        NivelPatrocinio nivelSeleccionado;

        try {

            nivelSeleccionado =
                    NivelPatrocinio.valueOf(
                            nivel
                    );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "El nivel de patrocinio seleccionado no es válido.",
                    "Alta Patrocinio",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // =================================================
        // CREAR DTO
        // =================================================

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

        // =================================================
        // ALTA
        // =================================================

        try {

            sistema.altaPatrocinio(dt);

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Patrocinio creado correctamente.",
                    "Alta Patrocinio",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarFormulario();
            accionCerrar.run();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    e.getMessage(),
                    "Alta Patrocinio",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Ocurrió un error inesperado al crear el patrocinio:\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // MENSAJE DE ÉXITO
    // =====================================================

    private void mostrarExito() {

        JOptionPane.showMessageDialog(
                mainPanel,
                "Patrocinio creado correctamente.",
                "Alta Patrocinio",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =====================================================
    // LIMPIAR FORMULARIO
    // =====================================================

    private void limpiarFormulario() {

        txtAporte.setText("");
        txtCantidadRegistros.setText("");
        txtCodigo.setText("");

        comboEvento.setSelectedIndex(-1);

        comboEdicion.removeAllItems();
        comboTipoRegistro.removeAllItems();

        comboInstitucion.setSelectedIndex(-1);
        comboNivel.setSelectedIndex(-1);
    }

    // =====================================================
    // REFRESCAR DATOS
    // =====================================================

    public void refrescarDatos() {

        cargarEventos();
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