package swing;

import datatypes.DtAltaTipoRegistro;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtTipoRegistro;
import implementacion.Fabrica;
import interfaces.ISistema;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

public class AltaTipoRegistro {
    private final transient ISistema sistema;


    private JPanel mainPanel;
    private JPanel principalPanel;
    private JComboBox cbxEvento;
    private JTextArea txtDescripcion;
    private JSpinner snrCosto;
    private JSpinner snrCupos;
    private JPanel botonesPanel;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private JComboBox cbxEdicion;
    private JTextField txtNombre;

    public AltaTipoRegistro()
    {
        sistema= Fabrica.getInstance().getISistema();
        snrCosto.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
        snrCupos.setModel(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
        configurarEventos();
        cargarEvento();

        cbxEvento.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                cargarEdicion();
            }
        });
    }

    private void cargarEvento() {
        Collection<DtEvento> listado=sistema.listarEventos();
        DefaultComboBoxModel<String>modelo=new DefaultComboBoxModel<>();
        for(DtEvento evento:listado)
        {
            modelo.addElement(evento.getNombre());
        }
        cbxEvento.setModel(modelo);
        cargarEdicion();
    }

    private void cargarEdicion() {
        String nombreEvento=(String)cbxEvento.getSelectedItem();
        Collection<DtEdicion> listado= sistema.obtenerEdicionesEvento(nombreEvento);
        DefaultComboBoxModel<String>modelo=new DefaultComboBoxModel<>();
        for(DtEdicion edicion:listado)
        {
            modelo.addElement(edicion.getIdNombre());
        }
        cbxEdicion.setModel(modelo);
    }


    private void configurarEventos() {
        btnAceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                altaTipoRegistro();
            }
        });
    }

    private void altaTipoRegistro() {
        if(cbxEvento.getSelectedItem()!=null) {

            if(cbxEdicion.getSelectedItem()!=null)
            {
                DtAltaTipoRegistro dtAltaTipoRegistro=convertirDtAltaRegistro();
                try {
                    sistema.altaTipoRegistro(dtAltaTipoRegistro);
                }
                catch(Exception e)
                {
                    JOptionPane.showMessageDialog(principalPanel,e.getMessage());
                }


            }
            else{
                JOptionPane.showMessageDialog(mainPanel,"Seleccione una edicion");
            }
        }
        else{
            JOptionPane.showMessageDialog(mainPanel,"Seleccione un evento");
        }



    }

    private DtAltaTipoRegistro convertirDtAltaRegistro() {
        DtAltaTipoRegistro dtAltaTipoRegistro=new DtAltaTipoRegistro();
        String nombreEdicion=cbxEdicion.getSelectedItem().toString();
        String nombre=txtNombre.getText();
        String descripcion=txtDescripcion.getText();
        float costo=Float.parseFloat(snrCosto.getValue().toString());
        int cupo=Integer.parseInt(snrCupos.getValue().toString());


        dtAltaTipoRegistro.setCosto(costo);
        dtAltaTipoRegistro.setCupo(cupo);
        dtAltaTipoRegistro.setDescripcion(descripcion);
        dtAltaTipoRegistro.setNombreEdicion(nombreEdicion);

        dtAltaTipoRegistro.setIdNombre(nombre);

        return dtAltaTipoRegistro;
    }
}
